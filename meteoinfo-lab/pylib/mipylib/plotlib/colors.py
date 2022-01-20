from org.meteoinfo.geometry.colors import Normalize as JNormalize
from org.meteoinfo.geometry.colors import LogNorm as JLogNorm
from org.meteoinfo.geometry.colors import BoundaryNorm as JBoundaryNorm
from org.meteoinfo.geometry.colors import ExtendType
from org.meteoinfo.ndarray import Array

import mipylib.numeric as np

__all__ = ['Normalize','LogNorm','BoundaryNorm']

class Normalize(object):
    """
    A class which, when called, linearly normalizes data into the
    ``[0.0, 1.0]`` interval.
    """

    def __init__(self, vmin=None, vmax=None, clip=False):
        """
        Parameters
        ----------
        vmin, vmax : float or None
            If *vmin* and/or *vmax* is not given, they are initialized from the
            minimum and maximum value, respectively, of the first input
            processed; i.e., ``__call__(A)`` calls ``autoscale_None(A)``.
        clip : bool, default: False
            If ``True`` values falling outside the range ``[vmin, vmax]``,
            are mapped to 0 or 1, whichever is closer, and masked values are
            set to 1.  If ``False`` masked values remain masked.
            Clipping silently defeats the purpose of setting the over, under,
            and masked colors in a colormap, so it is likely to lead to
            surprises; therefore the default is ``clip=False``.
        Notes
        -----
        Returns 0 if ``vmin == vmax``.
        """
        self._norm = JNormalize(vmin, vmax, clip)

    @property
    def vmin(self):
        return self._norm.getMinValue()

    @vmin.setter
    def vmin(self, value):
        if value != self._norm.getMinValue():
            self._norm.setMinValue(value)

    @property
    def vmax(self):
        return self._norm.getMaxValue()

    @vmax.setter
    def vmax(self, value):
        if value != self._norm.getMaxValue():
            self._norm.setMaxValue(value)

    @property
    def clip(self):
        return self._norm.getClip()

    @clip.setter
    def clip(self, value):
        if value != self._norm.getClip():
            self._norm.setClip(value)

    def __call__(self, value, clip=None):
        """
        Normalize *value* data in the ``[vmin, vmax]`` interval into the
        ``[0.0, 1.0]`` interval and return it.
        Parameters
        ----------
        value
            Data to normalize.
        clip : bool
            If ``None``, defaults to ``self.clip`` (which defaults to
            ``False``).
        Notes
        -----
        If not already initialized, ``self.vmin`` and ``self.vmax`` are
        initialized using ``self.autoscale_None(value)``.
        """
        if not clip is None:
            self._norm.setClip(clip)

        if isinstance(value, (list, tuple)):
            value = np.array(value)

        r = self._norm.apply(value._array)
        if isinstance(r, Array):
            return np.array(r)
        else:
            return r

    def inverse(self, value):
        if isinstance(value, (list, tuple)):
            value = np.array(value)

        r = self._norm.inverse(value._array)
        if isinstance(r, Array):
            return np.array(r)
        else:
            return r

class LogNorm(Normalize):
    """Normalize a given value to the 0-1 range on a log scale."""

    def __init__(self, vmin=None, vmax=None, clip=False):
        """
        Parameters
        ----------
        vmin, vmax : float or None
            If *vmin* and/or *vmax* is not given, they are initialized from the
            minimum and maximum value, respectively, of the first input
            processed; i.e., ``__call__(A)`` calls ``autoscale_None(A)``.
        clip : bool, default: False
            If ``True`` values falling outside the range ``[vmin, vmax]``,
            are mapped to 0 or 1, whichever is closer, and masked values are
            set to 1.  If ``False`` masked values remain masked.
            Clipping silently defeats the purpose of setting the over, under,
            and masked colors in a colormap, so it is likely to lead to
            surprises; therefore the default is ``clip=False``.
        Notes
        -----
        Returns 0 if ``vmin == vmax``.
        """
        self._norm = JLogNorm(vmin, vmax, clip)

class BoundaryNorm(Normalize):
    """
    Generate a colormap index based on discrete intervals.
    Unlike `Normalize` or `LogNorm`, `BoundaryNorm` maps values to integers
    instead of to the interval 0-1.
    Mapping to the 0-1 interval could have been done via piece-wise linear
    interpolation, but using integers seems simpler, and reduces the number of
    conversions back and forth between integer and floating point.
    """
    def __init__(self, boundaries, ncolors, clip=False, extend='neither'):
        """
        Parameters
        ----------
        boundaries : array-like
            Monotonically increasing sequence of at least 2 boundaries.
        ncolors : int
            Number of colors in the colormap to be used.
        clip : bool, optional
            If clip is ``True``, out of range values are mapped to 0 if they
            are below ``boundaries[0]`` or mapped to ``ncolors - 1`` if they
            are above ``boundaries[-1]``.
            If clip is ``False``, out of range values are mapped to -1 if
            they are below ``boundaries[0]`` or mapped to *ncolors* if they are
            above ``boundaries[-1]``.
        extend : {'neither', 'both', 'min', 'max'}, default: 'neither'
            Extend the number of bins to include one or both of the
            regions beyond the boundaries.
        Returns
        -------
        int16 scalar or array
        Notes
        -----
        *boundaries* defines the edges of bins, and data falling within a bin
        is mapped to the color with the same index.
        If the number of bins, including any extensions, is less than
        *ncolors*, the color index is chosen by linear interpolation, mapping
        the ``[0, nbins - 1]`` range onto the ``[0, ncolors - 1]`` range.
        """
        super(BoundaryNorm, self).__init__(vmin=boundaries[0], vmax=boundaries[-1], clip=clip)

        if isinstance(boundaries, (list, tuple)):
            boundaries = np.array(boundaries)

        extend = ExtendType.valueOf(extend.upper())
        self._norm = JBoundaryNorm(boundaries._array, ncolors, extend)