
"""Make thermodynamic diagrams.

Contain tools for making thermodynamic diagrams, including the base plotting class,
`SkewT`, derived `Stuve` and `Emagram` classes, and a class for making a `Hodograph`.
"""

from org.meteoinfo.chart.transform import Affine2D, IdentityTransform, BlendedTransform
from java.awt.geom import AffineTransform

import mipylib.numeric as np
from mipylib.plotlib import LineCollection
from ..calc import dry_lapse, moist_lapse, vapor_pressure, dewpoint, lcl, el
from .. import constants


class SkewTTransform(Affine2D):
    """Perform Skew transform for Skew-T plotting.

    This works in pixel space, so is designed to be applied after the normal plotting
    transformations.
    """

    def __init__(self, rotation):
        """Initialize skew transform.

        This needs a reference to the parent bounding box to do the appropriate math and
        to register it as a child so that the transform is invalidated and regenerated if
        the bounding box changes.
        """
        Affine2D.__init__(self)

        self._rot_factor = np.tan(np.deg2rad(rotation))
        self.affineTransform.shear(self._rot_factor, 0)


class SkewT:
    r"""Make Skew-T log-P plots of data.

    This class simplifies the process of creating Skew-T log-P plots. It handles requesting the
    appropriate skewed projection, and provides simplified wrappers to make it easy to plot data, a
    dd wind barbs, and add other lines to the plots (e.g. dry adiabats)

    Attributes
    ----------
    ax : `meteolib._axes.Axes`
        The underlying Axes instance, which can be used for calling additional
        plot functions

    """

    def __init__(self, fig=None, rotation=30, rect=None):
        r"""Create SkewT - logP plots.

        Parameters
        ----------
        fig : matplotlib.figure.Figure, optional
            Source figure to use for plotting. If none is given, a new
            :class:`matplotlib.figure.Figure` instance will be created.
        rotation : float or int, optional
            Controls the rotation of temperature relative to horizontal. Given
            in degrees counterclockwise from x-axis. Defaults to 30 degrees.
        rect : tuple[float, float, float, float], optional
            Rectangle (left, bottom, width, height) in which to place the axes. This
            allows the user to place the axes at an arbitrary point on the figure.

        """
        if fig is None:
            import mipylib.plotlib.miplot as plt
            fig = plt.figure(newfig=False)
        self._fig = fig

        if rect:
            self.ax = fig.add_axes(rect)
        else:
            self.ax = fig.add_axes()

        self._rotation = rotation
        self.transSkew = SkewTTransform(rotation)
        self.ax.transData = (self.ax.transScale.plus(self.ax.transLimits).
            plus(self.transSkew).plus(self.ax.transAxes))

        # Set the yaxis as inverted with log scaling
        self.ax.set_yscale('log')

        # Also takes care of inverting the y-axis
        self.ax.set_ylim(1050, 100)
        self.ax.set_xlim(-40, 50)
        self.ax.grid(True)
        self.ax.set_yticks([1000, 900, 800, 700, 600, 500, 400, 300, 200, 100])

        self.mixing_lines = None
        self.dry_adiabats = None
        self.moist_adiabats = None


    def plot(self, pressure, t, *args, **kwargs):
        r"""Plot data.

        Simple wrapper around plot so that pressure is the first (independent)
        input. This is essentially a wrapper around `plot`.

        Parameters
        ----------
        pressure : array-like
            pressure values
        t : array-like
            temperature values, can also be used for things like dew point
        args
            Other positional arguments
        kwargs
            Other keyword arguments

        Returns
        -------
        list[Line2D]
            lines plotted
        """
        # Skew-T logP plotting
        return self.ax.plot(t, pressure, *args, **kwargs)


    def plot_barbs(self, pressure, u, v, c=None, xloc=1.0, **kwargs):
        r"""Plot wind barbs.

        Adds wind barbs to the skew-T plot. This is a wrapper around the
        `barbs` command that adds to appropriate transform to place the
        barbs in a vertical line, located as a function of pressure.

        Parameters
        ----------
        pressure : array-like
            pressure values
        u : array-like
            U (East-West) component of wind
        v : array-like
            V (North-South) component of wind
        c : array-like, optional
            An optional array used to map colors to the barbs
        xloc : float, optional
            Position for the barbs, in normalized axes coordinates, where 0.0
            denotes far left and 1.0 denotes far right. Defaults to far right.
        kwargs
            Other keyword arguments to pass to :func:`~plotlib.barbs`

        Returns
        -------
        Barbs instance created

        """
        x = np.zeros_like(pressure)
        x = x + xloc
        trans = BlendedTransform(self.ax.transAxes, self.ax.transData)
        if c is None:
            b = self.ax.barbs(x, pressure, u, v, transform=trans, clip_on=False, **kwargs)
        else:
            b = self.ax.barbs(x, pressure, u, v, c, transform=trans, clip_on=False, **kwargs)

        return b


    def plot_dry_adiabats(self, t0=None, pressure=None, **kwargs):
        r"""Plot dry adiabats.

        Adds dry adiabats (lines of constant potential temperature) to the
        plot. The default style of these lines is dashed red lines with an alpha
        value of 0.5. These can be overridden using keyword arguments.

        Parameters
        ----------
        t0 : array-like, optional
            Starting temperature values in Kelvin. If none are given, they will be
            generated using the current temperature range at the bottom of
            the plot.
        pressure : array-like, optional
            Pressure values to be included in the dry adiabats. If not
            specified, they will be linearly distributed across the current
            plotted pressure range.
        kwargs
            Other keyword arguments to pass to :class:`LineCollection`

        Returns
        -------
        LineCollection
            instance created

        See Also
        --------
        :func:`~meteolib.calc.dry_lapse`
        :meth:`plot_moist_adiabats`
        :class:`LineCollection`

        """
        # Remove old lines
        if self.dry_adiabats:
            self.dry_adiabats.remove()

        # Determine set of starting temps if necessary
        if t0 is None:
            xmin, xmax = self.ax.get_xlim()
            t0 = np.arange(xmin, xmax + 1, 10)

        # Get pressure levels based on ylims if necessary
        if pressure is None:
            pressure = np.linspace(*self.ax.get_ylim())

        # Assemble into data for plotting
        t = (dry_lapse(pressure, t0[:, np.newaxis] + constants.degCtoK, 1000)
             - constants.degCtoK)
        linedata = [np.vstack((ti, pressure)).T for ti in t]

        # Add to plot
        kwargs.setdefault('colors', 'r')
        kwargs.setdefault('linestyle', '--')
        kwargs.setdefault('alpha', 0.5)
        self.dry_adiabats = self.ax.add_graphic(LineCollection(linedata, **kwargs))
        return self.dry_adiabats


    def plot_moist_adiabats(self, t0=None, pressure=None, **kwargs):
        r"""Plot moist adiabats.

        Adds saturated pseudo-adiabats (lines of constant equivalent potential
        temperature) to the plot. The default style of these lines is dashed
        blue lines with an alpha value of 0.5. These can be overridden using
        keyword arguments.

        Parameters
        ----------
        t0 : array-like, optional
            Starting temperature values in Kelvin. If none are given, they will be
            generated using the current temperature range at the bottom of
            the plot.
        pressure : array-like, optional
            Pressure values to be included in the moist adiabats. If not
            specified, they will be linearly distributed across the current
            plotted pressure range.
        kwargs
            Other keyword arguments to pass to :class:`LineCollection`

        Returns
        -------
        LineCollection
            instance created

        See Also
        --------
        :func:`~meteolib.calc.moist_lapse`
        :meth:`plot_dry_adiabats`
        :class:`LineCollection`

        """
        # Remove old lines
        if self.moist_adiabats:
            self.moist_adiabats.remove()

        # Determine set of starting temps if necessary
        if t0 is None:
            xmin, xmax = self.ax.get_xlim()
            t0 = np.concatenate((np.arange(xmin, 0, 10),
                                np.arange(0, xmax + 1, 5)))

        # Get pressure levels based on ylims if necessary
        if pressure is None:
            pressure = np.linspace(*self.ax.get_ylim())

        # Assemble into data for plotting
        t = moist_lapse(pressure, t0 + constants.degCtoK, 1000.) - constants.degCtoK
        linedata = [np.vstack((ti, pressure)).T for ti in t]

        # Add to plot
        kwargs.setdefault('colors', 'b')
        kwargs.setdefault('linestyle', '--')
        kwargs.setdefault('alpha', 0.5)
        self.moist_adiabats = self.ax.add_graphic(LineCollection(linedata, **kwargs))
        return self.moist_adiabats


    def plot_mixing_lines(self, mixing_ratio=None, pressure=None, **kwargs):
        r"""Plot lines of constant mixing ratio.

        Adds lines of constant mixing ratio (isohumes) to the
        plot. The default style of these lines is dashed green lines with an
        alpha value of 0.8. These can be overridden using keyword arguments.

        Parameters
        ----------
        mixing_ratio : array-like, optional
            Unitless mixing ratio values to plot. If none are given, default
            values are used.
        pressure : array-like, optional
            Pressure values to be included in the isohumes. If not
            specified, they will be linearly distributed across the current
            plotted pressure range up to 600 mb.
        kwargs
            Other keyword arguments to pass to :class:`LineCollection`

        Returns
        -------
        LineCollection
            instance created

        See Also
        --------
        :class:`LineCollection`

        """
        # Remove old lines
        if self.mixing_lines:
            self.mixing_lines.remove()

        # Default mixing level values if necessary
        if mixing_ratio is None:
            mixing_ratio = np.array([0.00005, 0.0001, 0.0002, 0.0004, 0.001, 0.002, 0.004, 0.007, 0.01,
                                     0.016, 0.024, 0.032])
        mixing_ratio = mixing_ratio.reshape(-1, 1)

        # Set pressure range if necessary
        if pressure is None:
            pressure = np.linspace(600, max(self.ax.get_ylim()))

        # Assemble data for plotting
        td = dewpoint(vapor_pressure(pressure, mixing_ratio)) - constants.degCtoK
        linedata = [np.vstack((t, pressure)).T for t in td]

        # Add to plot
        kwargs.setdefault('colors', 'g')
        kwargs.setdefault('linestyle', '--')
        kwargs.setdefault('alpha', 0.8)
        self.mixing_lines = self.ax.add_graphic(LineCollection(linedata, **kwargs))
        return self.mixing_lines


    def shade_area(self, y, x1, x2=0, which='both', **kwargs):
        r"""Shade area between two curves.

        Shades areas between curves. Area can be where one is greater or less than the other
        or all areas shaded.

        Parameters
        ----------
        y : array-like
            1-dimensional array of numeric y-values
        x1 : array-like
            1-dimensional array of numeric x-values
        x2 : array-like
            1-dimensional array of numeric x-values
        which : str
            Specifies if `positive`, `negative`, or `both` areas are being shaded.
            Will be overridden by where.
        kwargs
            Other keyword arguments to pass to :class:`PolyCollection`

        Returns
        -------
        :class:`PolyCollection`

        See Also
        --------
        :class:`PolyCollection`
        :meth:`fill_betweenx`

        """
        fill_properties = {'positive':
                               {'facecolor': 'red', 'alpha': 0.4, 'where': x1 > x2},
                           'negative':
                               {'facecolor': 'blue', 'alpha': 0.4, 'where': x1 < x2},
                           'both':
                               {'facecolor': 'green', 'alpha': 0.4, 'where': None}}

        try:
            fill_args = fill_properties[which]
            fill_args.update(kwargs)
        except KeyError:
            raise ValueError('Unknown option for which: {}'.format(which))

        arrs = y, x1, x2

        if fill_args['where'] is not None:
            arrs = arrs + (fill_args['where'],)
            fill_args.pop('where', None)

        fill_args['interpolate'] = True
        fill_args['edgecolor'] = None

        return self.ax.fill_betweenx(*arrs, **fill_args)


    def shade_cape(self, pressure, t, t_parcel, **kwargs):
        r"""Shade areas of Convective Available Potential Energy (CAPE).

        Shades areas where the parcel is warmer than the environment (areas of positive
        buoyancy.

        Parameters
        ----------
        pressure : array-like, hPa
            Pressure values
        t : array-like, degC
            Temperature values
        t_parcel : array-like, degC
            Parcel path temperature values
        limit_shading : bool
            Eliminate shading below the LCL or above the EL, default is True
        kwargs
            Other keyword arguments to pass to :class:`PolyCollection`

        Returns
        -------
        :class:`PolyCollection`

        See Also
        --------
        :class:`PolyCollection`
        :meth:`fill_betweenx`

        """
        pressure = np.asarray(pressure)
        t = np.asarray(t)
        return self.shade_area(pressure, t_parcel, t, which='positive', **kwargs)


    def shade_cin(self, pressure, t, t_parcel, dewpoint=None, **kwargs):
        r"""Shade areas of Convective INhibition (CIN).

        Shades areas where the parcel is cooler than the environment (areas of negative
        buoyancy). If `dewpoint` is passed in, negative area below the lifting condensation
        level or above the equilibrium level is not shaded.

        Parameters
        ----------
        pressure : array-like, hPa
            Pressure values
        t : array-like, degC
            Temperature values
        t_parcel : array-like, degC
            Parcel path temperature values
        dewpoint : array-like, degC
            Dew point values, optional
        kwargs
            Other keyword arguments to pass to :class:`PolyCollection`

        Returns
        -------
        :class:`PolyCollection`

        See Also
        --------
        :class:`PolyCollection`
        :meth:`fill_betweenx`

        """
        pressure = np.asarray(pressure)
        t = np.asarray(t)

        if dewpoint is not None:
            lcl_p, _ = lcl(pressure[0], t[0] + constants.degCtoK, dewpoint[0] + constants.degCtoK)
            el_p, _ = el(pressure, t + constants.degCtoK, dewpoint + constants.degCtoK, t_parcel + constants.degCtoK)
            idx = np.logical_and(pressure > el_p, pressure < lcl_p)
        else:
            idx = np.arange(0, len(pressure))
        return self.shade_area(pressure[idx], t_parcel[idx], t[idx], which='negative',
                               **kwargs)
