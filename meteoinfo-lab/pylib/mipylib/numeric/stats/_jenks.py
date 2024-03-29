from org.meteoinfo.math.stats import StatsUtil, Jenks

from ..core import numeric as np

__all__ = ["jenks_breaks", "JenksNaturalBreaks"]


class JenksNaturalBreaks(object):
    """
    A class that can be used to classify a sequence of numbers into groups (clusters) using Fisher-Jenks natural breaks.
    """

    def __init__(self, n_classes=6):
        """
        Parameters
        ----------
        n_classes : int
            The number of classes to be generated by the classifier.
        """
        self.n_classes = n_classes

    def __repr__(self):
        return "JenksNaturalBreaks(n_classes={})".format(self.n_classes)

    def __str__(self):
        return self.__repr__()

    def fit(self, x):
        """
        Parameters
        ----------
        x : array-like
            The sequence of numbers (integer/float) to be classified.
        """
        x = np.asarray(x)

        self._jenks = Jenks()
        self._jenks.addValues(x.asarray())
        self._breaks = self._jenks.computeBreaks(self.n_classes)
        self.breaks_ = np.array(self._breaks.getClassValues())
        self.inner_breaks_ = self.breaks_[1:-1]  # because inner_breaks is more
        self.labels_ = self.predict(x)
        self.groups_ = self.group(x)

    def predict(self, x):
        """
        Predicts the class of each element in x.

        Parameters
        ----------
        x : scalar or array-like

        Returns
        -------
        array
        """
        x = np.asarray(x)
        r = self._breaks.classOf(x.asarray())
        return np.array(r)

    def group(self, x):
        """
        Groups the elements in x into groups according to the classifier.

        Parameters
        ----------
        x : array-like
            The sequence of numbers (integer/float) to be classified.

        Returns
        -------
        list of numpy.array
            The list of groups that contains the values of x.
        """
        arr = np.array(x)
        groups_ = [arr[arr <= self.inner_breaks_[0]]]
        for idx in range(len(self.inner_breaks_))[:-1]:
            groups_.append(arr[(arr > self.inner_breaks_[idx]) & (arr <= self.inner_breaks_[idx + 1])])
        groups_.append(arr[arr > self.inner_breaks_[-1]])
        return groups_

    def goodness_of_variance_fit(self, x):
        """
        Parameters
        ----------
        x : array-like

        Returns
        -------
        float
            The goodness of variance fit.
        """
        gvf = self._breaks.gvf()
        return gvf

    def get_label_(self, val):
        """
        Compute the group label of the given value.

        Parameters
        ----------
        val : float
            The value to be classified.

        Returns
        -------
        int : The label of the value.
        """
        return self._breaks.classOf(val)


def jenks_breaks(a, n_classes, gvf=False):
    """
    Compute natural breaks (Fisher-Jenks algorithm) on a sequence of `values`,
    given `n_classes`, the number of desired class.

    :param a: (*array*) Input data array.
    :param n_classes: (*int*) The number of desired class.
    :param gvf: (*float*) Whether return gvf (Goodness of Variance Fit) value. Default is `False`.

    :return: (*array*) The computed break values, including minimum and maximum, in order
        to have all the bounds for building `n_classes` classes,
        so the returned list has a length of `n_classes` + 1.
    """
    if isinstance(a, (list, tuple)):
        a = np.array(a)

    if gvf:
        r = StatsUtil.jenksBreaksGvf(a._array, n_classes)
        return np.array(r[0]), r[1]
    else:
        r = StatsUtil.jenksBreaks(a._array, n_classes)
        return np.array(r)
