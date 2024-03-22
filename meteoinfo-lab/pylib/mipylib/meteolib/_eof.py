import mipylib.numeric as np
import warnings

__all__ = ['EOF','varimax']


class EOF(object):
    """EOF analysis"""

    def __init__(self, dataset, weights=None, center=True, ddof=1):
        """
        The EOF solution is computed at initialization time. Method
        calls are used to retrieve computed quantities.

        **Arguments:**

        *dataset*
            A `NDArray` with two or more dimensions containing the data to be analysed.
            The first dimension is assumed to represent time. Missing
            values are permitted, in the form of `nan` values. Missing values must be constant
            with time (e.g., values of an oceanographic field over land).

        **Optional arguments:**

        *weights*
            An array of weights whose shape is compatible with those of
            the input array *dataset*. The weights can have the same
            shape as *dataset* or a shape compatible with an array
            broadcast (i.e., the shape of the weights can can match the
            rightmost parts of the shape of the input array *dataset*).
            If the input array *dataset* does not require weighting then
            the value *None* may be used. Defaults to *None* (no
            weighting).

        *center*
            If *True*, the mean along the first axis of *dataset* (the
            time-mean) will be removed prior to analysis. If *False*,
            the mean along the first axis will not be removed. Defaults
            to *True* (mean is removed).

            The covariance interpretation relies on the input data being
            anomaly data with a time-mean of 0. Therefore, this option
            should usually be set to *True*. Setting this option to
            *True* has the useful side effect of propagating missing
            values along the time dimension, ensuring that a solution
            can be found even if missing values occur in different
            locations at different times.

        *ddof*
            'Delta degrees of freedom'. The divisor used to normalize
            the covariance matrix is *N - ddof* where *N* is the
            number of samples. Defaults to *1*.

        **Returns:**

        *solver*
            An `Eof` instance.
        """
        # Store the input data in an instance variable.
        if dataset.ndim < 2:
            raise ValueError('the input data set must be '
                             'at least two dimensional')
        self._data = dataset.copy()

        # Store information about the shape/size of the input data.
        self._records = self._data.shape[0]
        self._originalshape = self._data.shape[1:]
        channels = np.prod(self._originalshape)

        # Weight the data set according to weighting argument.
        if weights is not None:
            try:
                # The broadcast_arrays call returns a list, so the second index
                # is retained, but also we want to remove the time dimension
                # from the weights so the first index from the broadcast
                # array is taken.
                self._weights = np.broadcast_arrays(
                    self._data[0:1], weights)[1][0]
                self._data = self._data * self._weights
            except ValueError:
                raise ValueError('weight array dimensions are incompatible')
            except TypeError:
                raise TypeError('weights are not a valid type')
        else:
            self._weights = None

        # Remove the time mean of the input data unless explicitly told
        # not to by the "center" argument.
        self._centered = center
        if center:
            self._data = self._center(self._data)

        # Reshape to two dimensions (time, space) creating the design matrix.
        self._data = self._data.reshape([self._records, channels])

        # Find the indices of values that are not missing in one row. All the
        # rows will have missing values in the same places provided the
        # array was centered. If it wasn't then it is possible that some
        # missing values will be missed and the singular value decomposition
        # will produce not a number for everything.
        if not self._valid_nan(self._data):
            raise ValueError('missing values detected in different '
                             'locations at different times')
        nonMissingIndex = np.where(np.logical_not(np.isnan(self._data[0])))[0]

        # Remove missing values from the design matrix.
        dataNoMissing = self._data[:, nonMissingIndex]
        if dataNoMissing.size == 0:
            raise ValueError('all input data is missing')

        # Compute the singular value decomposition of the design matrix.
        try:
            A, Lh, E = np.linalg.svd(dataNoMissing, full_matrices=False)
        except (np.linalg.LinAlgError, ValueError):
            raise ValueError('error encountered in SVD, check that missing '
                             'values are in the same places at each time and '
                             'that all the values are not missing')

        # Singular values are the square-root of the eigenvalues of the
        # covariance matrix. Construct the eigenvalues appropriately and
        # normalize by N-ddof where N is the number of observations. This
        # corresponds to the eigenvalues of the normalized covariance matrix.
        self._ddof = ddof
        normfactor = float(self._records - self._ddof)
        self._L = Lh * Lh / normfactor

        # Store the number of eigenvalues (and hence EOFs) that were actually
        # computed.
        self.neofs = len(self._L)

        # Re-introduce missing values into the eigenvectors in the same places
        # as they exist in the input maps. Create an array of not-a-numbers
        # and then introduce data values where required. We have to use the
        # astype method to ensure the eigenvectors are the same type as the
        # input dataset since multiplication by np.NaN will promote to 64-bit.
        self._flatE = np.ones([self.neofs, channels],
                              dtype=self._data.dtype) * np.nan
        self._flatE = self._flatE.astype(self._data.dtype)
        self._flatE[:, nonMissingIndex] = E

        # Remove the scaling on the principal component time-series that is
        # implicitly introduced by using SVD instead of eigen-decomposition.
        # The PCs may be re-scaled later if required.
        self._P = A * Lh

    def _center(self, in_array):
        """Remove the mean of an array along the first dimension."""
        # Compute the mean along the first dimension.
        mean = in_array.mean(axis=0)

        # Return the input array with its mean along the first dimension
        # removed.
        return (in_array - mean)

    def _valid_nan(self, in_array):
        inan = np.isnan(in_array)
        return (inan.any(axis=0) == inan.all(axis=0)).all()

    def pcs(self, pcscaling=0, npcs=None):
        """Principal component time series (PCs).

        **Optional arguments:**

        *pcscaling*
            Set the scaling of the retrieved PCs. The following
            values are accepted:

            * *0* : Un-scaled PCs (default).
            * *1* : PCs are scaled to unit variance (divided by the
              square-root of their eigenvalue).
            * *2* : PCs are multiplied by the square-root of their
              eigenvalue.

        *npcs*
            Number of PCs to retrieve. Defaults to all the PCs. If the
            number of PCs requested is more than the number that are
            available, then all available PCs will be returned.

        **Returns:**

        *pcs*
            An array where the columns are the ordered PCs.

        **Examples:**

        All un-scaled PCs::

            pcs = solver.pcs()

        First 3 PCs scaled to unit variance::

            pcs = solver.pcs(npcs=3, pcscaling=1)

        """
        slicer = slice(0, npcs)
        if pcscaling == 0:
            # Do not scale.
            return self._P[:, slicer].copy()
        elif pcscaling == 1:
            # Divide by the square-root of the eigenvalue.
            return self._P[:, slicer] / np.sqrt(self._L[slicer])
        elif pcscaling == 2:
            # Multiply by the square root of the eigenvalue.
            return self._P[:, slicer] * np.sqrt(self._L[slicer])
        else:
            raise ValueError('invalid PC scaling option: '
                             '{!s}'.format(pcscaling))

    def eofs(self, eofscaling=0, neofs=None):
        """Empirical orthogonal functions (EOFs).

        **Optional arguments:**

        *eofscaling*
            Sets the scaling of the EOFs. The following values are
            accepted:

            * *0* : Un-scaled EOFs (default).
            * *1* : EOFs are divided by the square-root of their
              eigenvalues.
            * *2* : EOFs are multiplied by the square-root of their
              eigenvalues.

        *neofs*
            Number of EOFs to return. Defaults to all EOFs. If the
            number of EOFs requested is more than the number that are
            available, then all available EOFs will be returned.

        **Returns:**

        *eofs*
            An array with the ordered EOFs along the first dimension.

        **Examples:**

        All EOFs with no scaling::

            eofs = solver.eofs()

        The leading EOF with scaling applied::

            eof1 = solver.eofs(neofs=1, eofscaling=1)

        """
        if neofs is None or neofs > self.neofs:
            neofs = self.neofs
        slicer = slice(0, neofs)
        neofs = neofs or self.neofs
        flat_eofs = self._flatE[slicer].copy()
        if eofscaling == 0:
            # No modification. A copy needs to be returned in case it is
            # modified. If no copy is made the internally stored eigenvectors
            # could be modified unintentionally.
            rval = flat_eofs
        elif eofscaling == 1:
            # Divide by the square-root of the eigenvalues.
            rval = flat_eofs / np.sqrt(self._L[slicer])[:, np.newaxis]
        elif eofscaling == 2:
            # Multiply by the square-root of the eigenvalues.
            rval = flat_eofs * np.sqrt(self._L[slicer])[:, np.newaxis]
        else:
            raise ValueError('invalid eof scaling option: '
                             '{!s}'.format(eofscaling))

        return rval.reshape((neofs,) + self._originalshape)

    def eofs_correlation(self, neofs=None):
        """Correlation map EOFs.

        Empirical orthogonal functions (EOFs) expressed as the
        correlation between the principal component time series (PCs)
        and the time series of the `Eof` input *dataset* at each grid
        point.

        .. note::

            These are not related to the EOFs computed from the
            correlation matrix.

        **Optional argument:**

        *neofs*
            Number of EOFs to return. Defaults to all EOFs. If the
            number of EOFs requested is more than the number that are
            available, then all available EOFs will be returned.

        **Returns:**

        *eofs*
            An array with the ordered EOFs along the first dimension.

        **Examples:**

        All EOFs::

            eofs = solver.eofsAsCorrelation()

        The leading EOF::

            eof1 = solver.eofsAsCorrelation(neofs=1)

        """
        # Retrieve the specified number of PCs.
        pcs = self.pcs(npcs=neofs, pcscaling=1)
        # Compute the correlation of the PCs with the input field.
        c = correlation_map(
            pcs,
            self._data.reshape((self._records,) + self._originalshape))

        return c

    def eofs_covariance(self, neofs=None, pcscaling=1):
        """Covariance map EOFs.

        Empirical orthogonal functions (EOFs) expressed as the
        covariance between the principal component time series (PCs)
        and the time series of the `Eof` input *dataset* at each grid
        point.

        **Optional arguments:**

        *neofs*
            Number of EOFs to return. Defaults to all EOFs. If the
            number of EOFs requested is more than the number that are
            available, then all available EOFs will be returned.

        *pcscaling*
            Set the scaling of the PCs used to compute covariance. The
            following values are accepted:

            * *0* : Un-scaled PCs.
            * *1* : PCs are scaled to unit variance (divided by the
              square-root of their eigenvalue) (default).
            * *2* : PCs are multiplied by the square-root of their
              eigenvalue.

            The default is to divide PCs by the square-root of their
            eigenvalue so that the PCs are scaled to unit variance
            (option 1).

        **Returns:**

        *eofs*
            An array with the ordered EOFs along the first dimension.

        **Examples:**

        All EOFs::

            eofs = solver.eofsAsCovariance()

        The leading EOF::

            eof1 = solver.eofsAsCovariance(neofs=1)

        The leading EOF using un-scaled PCs::

            eof1 = solver.eofsAsCovariance(neofs=1, pcscaling=0)

        """
        pcs = self.pcs(npcs=neofs, pcscaling=pcscaling)
        # Divide the input data by the weighting (if any) before computing
        # the covariance maps.
        data = self._data.reshape((self._records,) + self._originalshape)
        if self._weights is not None:
            with warnings.catch_warnings():
                warnings.simplefilter('ignore', RuntimeWarning)
                data /= self._weights
        c = covariance_map(pcs, data, ddof=self._ddof)

        return c

    def eigenvalues(self, neigs=None):
        """Eigenvalues (decreasing variances) associated with each EOF.

        **Optional argument:**

        *neigs*
            Number of eigenvalues to return. Defaults to all
            eigenvalues. If the number of eigenvalues requested is more
            than the number that are available, then all available
            eigenvalues will be returned.

        **Returns:**

        *eigenvalues*
            An array containing the eigenvalues arranged largest to
            smallest.

        **Examples:**

        All eigenvalues::

            eigenvalues = solver.eigenvalues()

        The first eigenvalue::

            eigenvalue1 = solver.eigenvalues(neigs=1)

        """
        # Create a slicer and use it on the eigenvalue array. A copy must be
        # returned in case the slicer takes all elements, in which case a
        # reference to the eigenvalue array is returned. If this is modified
        # then the internal eigenvalues array would then be modified as well.
        slicer = slice(0, neigs)
        return self._L[slicer].copy()

    def variance_fraction(self, neigs=None):
        """Fractional EOF mode variances.

        The fraction of the total variance explained by each EOF mode,
        values between 0 and 1 inclusive.

        **Optional argument:**

        *neigs*
            Number of eigenvalues to return the fractional variance for.
            Defaults to all eigenvalues. If the number of eigenvalues
            requested is more than the number that are available, then
            fractional variances for all available eigenvalues will be
            returned.

        **Returns:**

        *variance_fractions*
            An array containing the fractional variances.

        **Examples:**

        The fractional variance represented by each EOF mode::

            variance_fractions = solver.varianceFraction()

        The fractional variance represented by the first EOF mode::

            variance_fraction_mode_1 = solver.VarianceFraction(neigs=1)

        """
        # Return the array of eigenvalues divided by the sum of the
        # eigenvalues.
        slicer = slice(0, neigs)
        return self._L[slicer] / self._L.sum()

    def total_anomaly_variance(self):
        """
        Total variance associated with the field of anomalies (the sum
        of the eigenvalues).

        **Returns:**

        *total_variance*
            A scalar value.

        **Example:**

        Get the total variance::

            total_variance = solver.totalAnomalyVariance()

        """
        # Return the sum of the eigenvalues.
        return self._L.sum()

    def northtest(self, neigs=None, vfscaled=False):
        """Typical errors for eigenvalues.

        The method of North et al. (1982) is used to compute the typical
        error for each eigenvalue. It is assumed that the number of
        times in the input data set is the same as the number of
        independent realizations. If this assumption is not valid then
        the result may be inappropriate.

        **Optional arguments:**

        *neigs*
            The number of eigenvalues to return typical errors for.
            Defaults to typical errors for all eigenvalues. If the
            number of eigenvalues requested is more than the number that
            are available, then typical errors for all available
            eigenvalues will be returned.

        *vfscaled*
            If *True* scale the errors by the sum of the eigenvalues.
            This yields typical errors with the same scale as the values
            returned by `Eof.varianceFraction`. If *False* then no
            scaling is done. Defaults to *False* (no scaling).

        **Returns:**

        *errors*
            An array containing the typical errors.

        **References**

        North G.R., T.L. Bell, R.F. Cahalan, and F.J. Moeng (1982)
        Sampling errors in the estimation of empirical orthogonal
        functions. *Mon. Weather. Rev.*, **110**, pp 669-706.

        **Examples:**

        Typical errors for all eigenvalues::

            errors = solver.northTest()

        Typical errors for the first 5 eigenvalues scaled by the sum of
        the eigenvalues::

            errors = solver.northTest(neigs=5, vfscaled=True)

        """
        slicer = slice(0, neigs)
        # Compute the factor that multiplies the eigenvalues. The number of
        # records is assumed to be the number of realizations of the field.
        factor = np.sqrt(2.0 / self._records)
        # If requested, allow for scaling of the eigenvalues by the total
        # variance (sum of the eigenvalues).
        if vfscaled:
            factor /= self._L.sum()
        # Return the typical errors.
        return self._L[slicer] * factor

    def varimax(self, eofs, **kwargs):
        """Rotation empirical orthogonal functions (REOFs).

        **Optional arguments:**

        *eofs*
            EOF data array.

        *kwargs*
            Parameters for varimax rotation computation.

        **Returns:**

        *reofs*
            An array with the ordered REOFs along the first dimension.
        """
        neofs = eofs.shape[0]
        channels = np.prod(self._originalshape)
        eofs = eofs.reshape(neofs, channels)
        eofs = eofs.T

        reofs = varimax(eofs, kwargs)[0]
        reofs = reofs.T
        reofs = reofs.reshape((neofs,) + self._originalshape)

        return reofs


def _check_flat_center(pcs, field):
    """
    Check PCs and a field for shape compatibility, flatten both to 2D,
    and center along the first dimension.

    This set of operations is common to both covariance and correlation
    calculations.

    """
    # Get the number of times in the field.
    records = field.shape[0]
    if records != pcs.shape[0]:
        # Raise an error if the field has a different number of times to the
        # PCs provided.
        raise ValueError("PCs and field must have the same first dimension")
    if len(pcs.shape) > 2:
        # Raise an error if the PCs are more than 2D.
        raise ValueError("PCs must be 1D or 2D")
    # Check if the field is 1D.
    if len(field.shape) == 1:
        originalshape = tuple()
        channels = 1
    else:
        # Record the shape of the field and the number of spatial elements.
        originalshape = field.shape[1:]
        channels = np.prod(originalshape)
    # Record the number of PCs.
    if len(pcs.shape) == 1:
        npcs = 1
        npcs_out = tuple()
    else:
        npcs = pcs.shape[1]
        npcs_out = (npcs,)
    # Create a flattened field so iterating over space is simple. Also do this
    # for the PCs to ensure they are 2D.
    field_flat = field.reshape([records, channels])
    pcs_flat = pcs.reshape([records, npcs])
    # Centre both the field and PCs in the time dimension.
    field_flat = field_flat - field_flat.mean(axis=0)
    pcs_flat = pcs_flat - pcs_flat.mean(axis=0)
    return pcs_flat, field_flat, npcs_out + originalshape


def correlation_map(pcs, field):
    """Correlation maps for a set of PCs and a spatial-temporal field.

    Given an array where the columns are PCs (e.g., as output from
    `~eofs.standard.Eof.pcs`) and an array containing spatial-temporal
    data where the first dimension represents time, one correlation map
    per PC is computed.

    The field must have the same temporal dimension as the PCs. Any
    number of spatial dimensions (including zero) are allowed in the
    field and there can be any number of PCs.

    **Arguments:**

    *pcs*
        PCs as the columns of an array.

    *field*
        Spatial-temporal field with time as the first dimension.

    **Returns:**

    *correlation_maps*
        An array with the correlation maps along the first dimension.

    **Example:**

    Compute correlation maps for each PC::

        pcs = solver.pcs(pcscaling=1)
        correlation_maps = correlation_maps(pcs, field)

    """
    # Check PCs and fields for validity, flatten the arrays ready for the
    # computation and remove the mean along the leading dimension.
    pcs_cent, field_cent, out_shape = _check_flat_center(pcs, field)
    # Compute the standard deviation of the PCs and the fields along the time
    # dimension (the leading dimension).
    pcs_std = pcs_cent.std(axis=0)
    field_std = field_cent.std(axis=0)
    # Set the divisor.
    div = float(pcs_cent.shape[0])
    # Compute the correlation map.
    cor = np.dot(field_cent.T, pcs_cent).T / div
    cor /= np.outer(pcs_std, field_std)
    # Return the correlation with the appropriate shape.
    return cor.reshape(out_shape)

def covariance_map(pcs, field, ddof=1):
    """Covariance maps for a set of PCs and a spatial-temporal field.

    Given an array where the columns are PCs (e.g., as output from
    `eofs.standard.Eof.pcs`) and an array containing spatial-temporal
    data where the first dimension represents time, one covariance map
    per PC is computed.

    The field must have the same temporal dimension as the PCs. Any
    number of spatial dimensions (including zero) are allowed in the
    field and there can be any number of PCs.

    **Arguments:**

    *pcs*
        PCs as the columns of an array.

    *field*
        Spatial-temporal field with time as the first dimension.

    **Optional arguments:**

    *ddof*
        'Delta degrees of freedom'. The divisor used to normalize
        the covariance matrix is *N - ddof* where *N* is the
        number of samples. Defaults to *1*.

    **Returns:**

    *covariance_maps*
        An array with the covariance maps along the first dimension.

    **Example:**

    Compute covariance maps for each PC::

        pcs = solver.pcs(pcscaling=1)
        covariance_maps = covariance_maps(pcs, field)

    """
    # Check PCs and fields for validity, flatten the arrays ready for the
    # computation and remove the mean along the leading dimension.
    pcs_cent, field_cent, out_shape = _check_flat_center(pcs, field)
    # Set the divisor according to the specified delta-degrees of freedom.
    div = float(pcs_cent.shape[0] - ddof)
    # Compute the covariance map, making sure it has the appropriate shape.
    cov = (np.dot(field_cent.T, pcs_cent).T / div).reshape(out_shape)
    return cov

def varimax(x, norm=True, tol=1e-10, it_max=1000):
    """
    Rotate EOFs according to varimax algorithm

    :param x: (*array_like*) Input 2-D array.
    :param norm: (*boolean*) Determines whether to do Kaiser normalization the rows
        of the loadings before performing the rotation. Default is `True`.
    :param tol: (*float*) Tolerance.
    :param it_max: (*int*) Specifies the maximum number of iterations to do.

    :returns: Rotated EOFs and rotate matrix.
    """
    has_nan = False
    if x.contains_nan():       #Has NaN value
        mask = np.isnan(x).sum(axis=1)
        valid_idx = np.where(mask==0)[0]
        xx = x[valid_idx,:]
        has_nan = True
    else:
        xx = x.copy()

    if norm:
        h = np.sqrt(np.sum(xx**2, axis=1))
        xx = xx / h[:, None]

    p, nc = xx.shape
    TT = np.eye(nc)
    d = 0
    for _ in range(it_max):
        z = np.dot(xx, TT)
        B = np.dot(xx.T, (z**3 - np.dot(z, np.diag(np.squeeze(np.dot(np.ones((1,p)), (z**2))))) / p))
        U, S, Vh = np.linalg.svd(B)
        TT = np.dot(U, Vh)
        d2 = d
        d = np.sum(S)
        # End if exceeded tolerance.
        if d < d2 * (1 + tol):
            break

    # Final matrix.
    r = np.dot(xx, TT)

    if norm:
        r = r * h[:,None]

    if has_nan:
        rr = np.ones(x.shape) * np.nan
        rr[valid_idx,:] = r
        r = rr

    return r, TT
