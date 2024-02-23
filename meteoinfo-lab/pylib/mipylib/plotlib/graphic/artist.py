from abc import ABCMeta, abstractmethod


__all__ = ['Artist']


def _stale_axes_callback(self, val):
    if self.axes:
        self.axes.stale = val

class Artist(object):
    """
    Abstract base class for graphic artist.
    """

    __metaclass__ = ABCMeta

    def __init__(self):
        self._axes = None
        self._figure = None
        self._stale = True
        self.stale_callback = None
        self._animated = False

    @property
    def axes(self):
        """The `~.axes.Axes` instance the artist resides in, or *None*."""
        return self._axes

    @axes.setter
    def axes(self, new_axes):
        if (new_axes is not None and self._axes is not None
                and new_axes != self._axes):
            raise ValueError("Can not reset the axes.  You are probably "
                             "trying to re-use an artist in more than one "
                             "Axes which is not supported")
        self._axes = new_axes
        if new_axes is not None and new_axes is not self:
            self.stale_callback = _stale_axes_callback

    @property
    def stale(self):
        """
        Whether the artist is 'stale' and needs to be re-drawn for the output
        to match the internal state of the artist.
        """
        return self._stale

    @stale.setter
    def stale(self, val):
        self._stale = val

        # if the artist is animated it does not take normal part in the
        # draw stack and is not expected to be drawn as part of the normal
        # draw loop (when not saving) so do not propagate this change
        if self._animated:
            return

        if val and self.stale_callback is not None:
            self.stale_callback(self, val)