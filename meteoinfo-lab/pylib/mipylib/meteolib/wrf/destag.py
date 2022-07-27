
__all__ = ['destagger']

def destagger(var, stagger_dim):
    """
    Return the variable data array on the unstaggered grid.

    This function destaggers the variable by taking the average of the
    values located on either side of the grid box.

    :param var: (*array*) A variable on a staggered grid.
    :param stagger_dim: (*int*) The dimension index to destagger.
        Negative values can be used to choose dimensions referenced
        from the right hand side (-1 is the rightmost dimension).

    :returns: (*array*) The destaggered variable data array.
    """
    var_shape = var.shape
    num_dims = var.ndim
    stagger_dim_size = var_shape[stagger_dim]
    # Dynamically building the range slices to create the appropriate
    # number of ':'s in the array accessor lists.
    # For example, for a 3D array, the calculation would be
    # result = .5 * (var[:,:,0:stagger_dim_size-2]
    #                    + var[:,:,1:stagger_dim_size-1])
    # for stagger_dim=2.  So, full slices would be used for dims 0 and 1, but
    # dim 2 needs the special slice.
    full_slice = slice(None)
    slice1 = slice(0, stagger_dim_size - 1, 1)
    slice2 = slice(1, stagger_dim_size, 1)

    # default to full slices
    dim_ranges_1 = [full_slice] * num_dims
    dim_ranges_2 = [full_slice] * num_dims

    # for the stagger dim, insert the appropriate slice range
    dim_ranges_1[stagger_dim] = slice1
    dim_ranges_2[stagger_dim] = slice2

    result = .5*(var[tuple(dim_ranges_1)] + var[tuple(dim_ranges_2)])

    return result