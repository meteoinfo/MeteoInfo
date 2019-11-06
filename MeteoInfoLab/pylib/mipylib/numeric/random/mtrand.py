from org.meteoinfo.math.random import MTRandom

from ..core import NDArray
from ..core import numeric as np

class RandomState(object):
    '''
    RandomState(seed=None)

    Container for the slow Mersenne Twister pseudo-random number generator

    :param seed: (*int*) optional
        Random seed used to initialize the pseudo-random number generator or
        an instantized BitGenerator.
    '''

    def __init__(self, seed=None):
        self._seed = seed
        if seed is None:
            self._mtrand = MTRandom()
        else:
            self._mtrand = MTRandom(seed)

    def rand(*args):
        """
        Random values in a given shape.

        Create an array of the given shape and propagate it with random samples from a uniform
            distribution over [0, 1).

        :param d0, d1, ..., dn: (*int*) optional. The dimensions of the returned array, should all
            be positive. If no argument is given a single Python float is returned.

        :returns: Random values array.
        """
        if len(args) == 0:
            return self._mtrand.rand()
        elif len(args) == 1:
            return NDArray(self._mtrand.rand(args[0]))
        else:
            return NDArray(self._mtrand.rand(args))

    def shuffle(self, x):
        '''
        Modify a sequence in-place by shuffling its contents.

        This function only shuffles the array along the first axis of a
        multi-dimensional array. The order of sub-arrays is changed but
        their contents remains the same.

        :param x: (*array*) Input array
        :return: None
        '''
        self._mtrand.shuffle(x._array)

    def permutation(self, x):
        '''
        MRandomly permute a sequence, or return a permuted range.

        If x is a multi-dimensional array, it is only shuffled along its first index.

        :param x: (*array*) Input array

        :return: Permutation array
        '''
        if isinstance(x, int):
            arr = np.arange(x)
        else:
            x = np.asanyarray(x)
            arr = x.copy()
        self._mtrand.shuffle(arr._array)
        return arr

_rand = RandomState()