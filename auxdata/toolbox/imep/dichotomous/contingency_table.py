from .._base import VerifyTable

class ContingencyTable(VerifyTable):

    def __init__(self, obs, fcst, name="Contingency"):
        """
        Contingency table class that shows the frequency of "yes" and "no" forecasts and occurrences.

        :param obs: (*array*) Observation data.
        :param fcst: (*array*) Forecasting data.
        :param name: (*str*) The table name.
        """
        pass