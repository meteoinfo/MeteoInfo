"""
``numeric.lib`` is mostly a space for implementing functions that don't
belong in core or in another numeric submodule with a clear purpose
"""

from .shape_base import *
from .function_base import *
from .index_tricks import *
from .stride_tricks import *
from .type_check import *
from .arraysetops import *
from .npyio import *
from .matio import *
from .polynomial import *

__all__ = []
__all__ += shape_base.__all__
__all__ += function_base.__all__
__all__ += index_tricks.__all__
__all__ += stride_tricks.__all__
__all__ += type_check.__all__
__all__ += arraysetops.__all__
__all__ += npyio.__all__
__all__ += matio.__all__
__all__ += polynomial.__all__