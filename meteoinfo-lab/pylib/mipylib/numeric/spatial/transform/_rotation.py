
from org.apache.commons.geometry.euclidean.threed.rotation import QuaternionRotation, AxisReferenceFrame, \
    AxisSequence, AxisAngleSequence
from org.apache.commons.geometry.euclidean.threed import Vector3D
from org.meteoinfo.math.spatial.transform import TransformUtil

import re
from numbers import Number
from mipylib import numeric as np


def _cross3(a, b):
    result = np.empty(3)
    result[0] = a[1]*b[2] - a[2]*b[1]
    result[1] = a[2]*b[0] - a[0]*b[2]
    result[2] = a[0]*b[1] - a[1]*b[0]
    return result

def _compose_quat_single(p, q, r):
    # calculate p * q into r
    cross = _cross3(p[:3], q[:3])

    r[0] = p[3]*q[0] + q[3]*p[0] + cross[0]
    r[1] = p[3]*q[1] + q[3]*p[1] + cross[1]
    r[2] = p[3]*q[2] + q[3]*p[2] + cross[2]
    r[3] = p[3]*q[3] - p[0]*q[0] - p[1]*q[1] - p[2]*q[2]

def _compose_quat(p, q):
    n = max(p.shape[0], q.shape[0])
    product = np.empty((n, 4))

    # dealing with broadcasting
    if p.shape[0] == 1:
        for ind in range(n):
            _compose_quat_single(p[0], q[ind], product[ind])
    elif q.shape[0] == 1:
        for ind in range(n):
            _compose_quat_single(p[ind], q[0], product[ind])
    else:
        for ind in range(n):
            _compose_quat_single(p[ind], q[ind], product[ind])

    return product

def _make_elementary_quat(axis, angles):
    n = angles.shape[0]
    quat = np.zeros((n, 4))

    axis_ind = 0
    if axis == b'x':   axis_ind = 0
    elif axis == b'y': axis_ind = 1
    elif axis == b'z': axis_ind = 2

    for ind in range(n):
        quat[ind, 3] = cos(angles[ind] / 2.)
        quat[ind, axis_ind] = sin(angles[ind] / 2.)
    return quat

def _elementary_quat_compose(seq, angles, intrinsic=False):
    result = _make_elementary_quat(seq[0], angles[:, 0])
    seq_len = seq.shape[0]

    for idx in range(1, seq_len):
        if intrinsic:
            result = _compose_quat(
                result,
                _make_elementary_quat(seq[idx], angles[:, idx]))
        else:
            result = _compose_quat(
                _make_elementary_quat(seq[idx], angles[:, idx]),
                result)
    return result

def _format_angles(angles, degrees, num_axes):
    angles = np.asarray(angles, dtype='float')
    if degrees:
        angles = np.deg2rad(angles)

    is_single = False
    # Prepare angles to have shape (num_rot, num_axes)
    if num_axes == 1:
        if angles.ndim == 0:
            # (1, 1)
            angles = angles.reshape((1, 1))
            is_single = True
        elif angles.ndim == 1:
            # (N, 1)
            angles = angles[:, None]
        elif angles.ndim == 2 and angles.shape[-1] != 1:
            raise ValueError("Expected `angles` parameter to have shape "
                             "(N, 1), got {}.".format(angles.shape))
        elif angles.ndim > 2:
            raise ValueError("Expected float, 1D array, or 2D array for "
                             "parameter `angles` corresponding to `seq`, "
                             "got shape {}.".format(angles.shape))
    else:  # 2 or 3 axes
        if angles.ndim not in [1, 2] or angles.shape[-1] != num_axes:
            raise ValueError("Expected `angles` to be at most "
                             "2-dimensional with width equal to number "
                             "of axes specified, got "
                             "{} for shape".format(angles.shape))

        if angles.ndim == 1:
            # (1, num_axes)
            angles = angles[None, :]
            is_single = True

    # By now angles should have shape (num_rot, num_axes)
    # sanity check
    if angles.ndim != 2 or angles.shape[-1] != num_axes:
        raise ValueError("Expected angles to have shape (num_rotations, "
                         "num_axes), got {}.".format(angles.shape))

    return angles, is_single

def _get_axis_vector(axis):
    if axis == b'x':   return Vector3D.of(1, 0, 0)
    elif axis == b'y': return Vector3D.of(0, 1, 0)
    elif axis == b'z': return Vector3D.of(0, 0, 1)
    else:              return Vector3D.of(1, 0, 0)

def _get_axis_sequence(seq):
    if seq == 'xyz':   return AxisSequence.XYZ
    elif seq == 'xzy': return AxisSequence.XZY
    elif seq == 'yxz': return AxisSequence.YXZ
    elif seq == 'yzx': return AxisSequence.YZX
    elif seq == 'zxy': return AxisSequence.ZXY
    elif seq == 'zyx': return AxisSequence.ZYX
    elif seq == 'xyx': return AxisSequence.XYX
    elif seq == 'xzx': return AxisSequence.XZX
    elif seq == 'yxy': return AxisSequence.YXY
    elif seq == 'yzy': return AxisSequence.YZY
    elif seq == 'zxz': return AxisSequence.ZXZ
    elif seq == 'zyz': return AxisSequence.ZYZ
    else:              return AxisSequence.XYZ

class Rotation(object):

    def __init__(self, rotation=None):
        if rotation is None:
            self._rotation = QuaternionRotation()
        else:
            self._rotation = rotation

    @classmethod
    def from_euler(cls, seq, angles, degrees=False):
        """Initialize from Euler angles.

        Rotations in 3-D can be represented by a sequence of 3
        rotations around a sequence of axes. In theory, any three axes spanning
        the 3-D Euclidean space are enough. In practice, the axes of rotation are
        chosen to be the basis vectors.

        The three rotations can either be in a global frame of reference
        (extrinsic) or in a body centred frame of reference (intrinsic), which
        is attached to, and moves with, the object under rotation [1]_.

        Parameters
        ----------
        seq : string
            Specifies sequence of axes for rotations. Up to 3 characters
            belonging to the set {'X', 'Y', 'Z'} for intrinsic rotations, or
            {'x', 'y', 'z'} for extrinsic rotations. Extrinsic and intrinsic
            rotations cannot be mixed in one function call.
        angles : float or array_like, shape (N,) or (N, [1 or 2 or 3])
            Euler angles specified in radians (`degrees` is False) or degrees
            (`degrees` is True).
            For a single character `seq`, `angles` can be:

            - a single value
            - array_like with shape (N,), where each `angle[i]`
              corresponds to a single rotation
            - array_like with shape (N, 1), where each `angle[i, 0]`
              corresponds to a single rotation

            For 2- and 3-character wide `seq`, `angles` can be:

            - array_like with shape (W,) where `W` is the width of
              `seq`, which corresponds to a single rotation with `W` axes
            - array_like with shape (N, W) where each `angle[i]`
              corresponds to a sequence of Euler angles describing a single
              rotation

        degrees : bool, optional
            If True, then the given angles are assumed to be in degrees.
            Default is False.

        Returns
        -------
        rotation : `Rotation` instance
            Object containing the rotation represented by the sequence of
            rotations around given axes with given angles.
        """
        num_axes = len(seq)
        if num_axes < 1 or num_axes > 3:
            raise ValueError("Expected axis specification to be a non-empty "
                             "string of upto 3 characters, got {}".format(seq))

        intrinsic = (re.match(r'^[XYZ]{1,3}$', seq) is not None)
        extrinsic = (re.match(r'^[xyz]{1,3}$', seq) is not None)
        if not (intrinsic or extrinsic):
            raise ValueError("Expected axes from `seq` to be from ['x', 'y', "
                             "'z'] or ['X', 'Y', 'Z'], got {}".format(seq))

        if any(seq[i] == seq[i+1] for i in range(num_axes - 1)):
            raise ValueError("Expected consecutive axes to be different, "
                             "got {}".format(seq))

        seq = seq.lower()

        angles = np.asarray(angles, dtype='float')
        if degrees:
            angles = np.deg2rad(angles)

        if angles.ndim == 0:
            axis_vector = _get_axis_vector(seq)
            rotation = QuaternionRotation.fromAxisAngle(axis_vector, angles.item())
        else:
            arf = AxisReferenceFrame.RELATIVE if intrinsic else AxisReferenceFrame.ABSOLUTE
            axis_sequence = _get_axis_sequence(seq)
            aas = AxisAngleSequence(arf, axis_sequence, angles[0], angles[1], angles[2])
            rotation = QuaternionRotation.fromAxisAngleSequence(aas)
        return cls(rotation)

    def apply(self, vectors, inverse=False):
        """Apply this rotation to a set of vectors.

        If the original frame rotates to the final frame by this rotation, then
        its application to a vector can be seen in two ways:

            - As a projection of vector components expressed in the final frame
              to the original frame.
            - As the physical rotation of a vector being glued to the original
              frame as it rotates. In this case the vector components are
              expressed in the original frame before and after the rotation.

        In terms of rotation matrices, this application is the same as
        ``self.as_matrix() @ vectors``.

        Parameters
        ----------
        vectors : array_like, shape (3,) or (N, 3)
            Each `vectors[i]` represents a vector in 3D space. A single vector
            can either be specified with shape `(3, )` or `(1, 3)`. The number
            of rotations and number of vectors given must follow standard numpy
            broadcasting rules: either one of them equals unity or they both
            equal each other.
        inverse : boolean, optional
            If True then the inverse of the rotation(s) is applied to the input
            vectors. Default is False.

        Returns
        -------
        rotated_vectors : ndarray, shape (3,) or (N, 3)
            Result of applying rotation on input vectors.
            Shape depends on the following cases:

                - If object contains a single rotation (as opposed to a stack
                  with a single rotation) and a single vector is specified with
                  shape ``(3,)``, then `rotated_vectors` has shape ``(3,)``.
                - In all other cases, `rotated_vectors` has shape ``(N, 3)``,
                  where ``N`` is either the number of rotations or vectors.
        """
        vectors = np.asarray(vectors)
        if vectors.ndim > 2 or vectors.shape[-1] != 3:
            raise ValueError("Expected input of shape (3,) or (P, 3), "
                             "got {}.".format(vectors.shape))

        r = TransformUtil.rotation(self._rotation, vectors._array)
        return np.NDArray(r)
