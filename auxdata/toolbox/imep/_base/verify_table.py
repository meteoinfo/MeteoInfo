
class VerifyTable(object):

    def __init__(self, name='Default'):
        """
        Verification table class.
        :param name: (*str*) Table name.
        """
        self.name = name

    @abstractmethod
    def get_scores(self):
        """
        Get verification scores.
        :return: Verification scores.
        """
        pass