
import mipylib.numeric as np

def in_bounds(x, lb, ub):
    """Check if a point lies within bounds."""
    return np.all((x >= lb) & (x <= ub))