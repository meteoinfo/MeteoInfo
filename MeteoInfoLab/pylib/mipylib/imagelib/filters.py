# coding=utf-8
#-----------------------------------------------------
# Author: Yaqiang Wang
# Date: 2017-5-25
# Purpose: MeteoInfoLab filters module in image package
# Note: Jython
#-----------------------------------------------------

from org.meteoinfo.shape import Graphic
from org.meteoinfo.image import ImageUtil
from mipylib.geolib.milayer import MILayer
from mipylib.numeric.miarray import MIArray
from org.meteoinfo.image.filter import ContrastFilter, SharpenFilter, RGBAdjustFilter, ChannelMixFilter, \
    GainFilter, GammaFilter, GrayFilter, GrayscaleFilter, HSBAdjustFilter, InvertAlphaFilter, \
    InvertFilter, LevelsFilter, MaskFilter, PosterizeFilter, RescaleFilter, SolarizeFilter, \
    ThresholdFilter, FlipFilter, RotateFilter, EmbossFilter, TritoneFilter, LightFilter, OpacityFilter
from java.awt.image import BufferedImage
import math

__all__ = [
    'contrast','sharpen','rgb_adjust','channel_mix','gain','gamma','gray','gray_scale',
    'hsb_adjust','invert_alpha','invert','levels','mask','posterize','rescale','solarize',
    'threshold','tritone','flip','rotate','emboss','light','opacity','count','mean'
    ]

def __getimage(src):
    if isinstance(src, BufferedImage):
        return src
    elif isinstance(src, Graphic):
        return src.getShape().getImage()
    elif isinstance(src, MILayer):
        return src.layer.getImage()
    elif isinstance(src, MIArray):
        return ImageUtil.createImage(src.asarray())
    return None
    
def __getreturn(src, dst):
    if isinstance(src, Graphic):
        src.getShape().setImage(dst)
        return src
    elif isinstance(src, MILayer):
        src.layer.setImage(dst)
        return src
    elif isinstance(src, MIArray):
        r = ImageUtil.imageRead(dst)
        return MIArray(r)
    else:
        return dst
    
def contrast(src, brightness=1, contrast=1):
    '''
    A filter to change the brightness and contrast of an image.
    
    :param src: (*image*) Source image.
    :param brightness: (*float*) Brightness.
    :param contrast: (*float*) Contrast.
    
    :returns: Destination image.
    '''
    image = __getimage(src)
    if image is None:
        return None
        
    filter = ContrastFilter()  
    filter.setBrightness(brightness)
    filter.setContrast(contrast)
    dst = BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB)
    filter.filter(image, dst)    
    return __getreturn(src, dst)
    
def sharpen(src):
    '''
    A filter which performs a simple 3x3 sharpening operation.
    
    :param src: (*image*) Source image.
    
    :returns: Destination image.
    '''
    image = __getimage(src)
    if image is None:
        return None
        
    filter = SharpenFilter()  
    dst = BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB)
    filter.filter(image, dst)
    return __getreturn(src, dst)
    
def rgb_adjust(src, r=0, g=0, b=0):
    '''
    This filter adds or subtracts a given amount from each of the red, green and blue channels 
    of an image.
    
    :param src: (*image*) Source image.
    :param r: (*float*) Red channel.
    :param g: (*float*) Green channel.
    :param b: (*float*) Blue channel.
    
    :returns: Destination image.
    '''
    image = __getimage(src)
    if image is None:
        return None
        
    filter = RGBAdjustFilter(r, g, b)      
    dst = BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB)
    filter.filter(image, dst)
    return __getreturn(src, dst)
    
def channel_mix(src, b_g=0, r_b=0, g_r=0, to_r=0, to_g=0, to_b=0):
    '''
    A filter which allows the red, green and blue channels of an image to be mixed into each other.
    
    :param src: (*image*) Source image.
    :param b_g: (*float*) Blue and green.
    :param r_b: (*float*) Red and blue.
    :param g_r: (*float*) Green and red.
    :param to_r: (*float*) Mix into red.
    :param to_g: (*float*) Mix into green.
    :param to_b: (*float*) Mix into blue.
    
    :returns: Destination image.
    '''
    image = __getimage(src)
    if image is None:
        return None
        
    filter = ChannelMixFilter()      
    filter.setBlueGreen(b_g)
    filter.setRedBlue(r_b)
    filter.setGreenRed(g_r)
    filter.setIntoR(to_r)
    filter.setIntoG(to_g)
    filter.setIntoB(to_b)
    dst = BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB)
    filter.filter(image, dst)
    return __getreturn(src, dst)
    
def gain(src, gain=0.5, bias=0.5):
    '''
    A filter which changes the gain and bias of an image
    
    :param src: (*image*) Source image.
    :param grain: (*float*) Gain.
    :param bias: (*float*) Bias.
    
    :returns: Destination image.
    '''
    image = __getimage(src)
    if image is None:
        return None
        
    filter = GainFilter()      
    filter.setGain(gain)
    filter.setBias(bias)
    dst = BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB)
    filter.filter(image, dst)
    return __getreturn(src, dst)
    
def gamma(src, gamma=None, rgamma=1, ggamma=1, bgamma=1):
    '''
    A filter for changing the gamma of an image.
    
    :param src: (*image*) Source image.
    :param gamma: (*float*) Gamma value.
    :param rgamma: (*float*) Red gamma value.
    :param ggamma: (*float*) Green gamma value.
    :param bgamma: (*float*) Blue gamma value.
    
    :returns: Destination image.
    '''
    image = __getimage(src)
    if image is None:
        return None
            
    if gamma is None:
        filter = GammaFilter(rgamma, ggamma, bgamma)
    else:
        filter = GammaFilter(gamma)
    dst = BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB)
    filter.filter(image, dst)
    return __getreturn(src, dst)
    
def gray(src):
    '''
    A filter which 'grays out' an image by averaging each pixel with white.
    
    :param src: (*image*) Source image.
    
    :returns: Destination image.
    '''
    image = __getimage(src)
    if image is None:
        return None
            
    filter = GrayFilter()
    dst = BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB)
    filter.filter(image, dst)
    return __getreturn(src, dst)
    
def gray_scale(src):
    '''
    A filter which converts an image to grayscale using the NTSC brightness calculation.
    
    :param src: (*image*) Source image.
    
    :returns: Destination image.
    '''
    image = __getimage(src)
    if image is None:
        return None
            
    filter = GrayscaleFilter()
    dst = BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB)
    filter.filter(image, dst)
    return __getreturn(src, dst)
    
def hsb_adjust(src, h=0, s=0, b=0):
    '''
    This filter adds or subtracts a given amount from each of the hue, saturation and brightness 
    channels of an image.
    
    :param src: (*image*) Source image.
    :param h: (*float*) Hue.
    :param s: (*float*) Saturation.
    :param b: (*float*) brightness.
    
    :returns: Destination image.
    '''
    image = __getimage(src)
    if image is None:
        return None
        
    filter = HSBAdjustFilter(h, s, b)      
    dst = BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB)
    filter.filter(image, dst)
    return __getreturn(src, dst)
    
def invert_alpha(src):
    '''
    A Filter to invert the alpha channel of an image.
    
    :param src: (*image*) Source image.
    
    :returns: Destination image.
    '''
    image = __getimage(src)
    if image is None:
        return None
            
    filter = InvertAlphaFilter()
    dst = BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB)
    filter.filter(image, dst)
    return __getreturn(src, dst)
    
def invert(src):
    '''
    A filter which inverts the RGB channels of an image.
    
    :param src: (*image*) Source image.
    
    :returns: Destination image.
    '''
    image = __getimage(src)
    if image is None:
        return None
            
    filter = InvertFilter()
    dst = BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB)
    filter.filter(image, dst)
    return __getreturn(src, dst)
    
def levels(src, low=0, high=1, low_out=0, high_out=1):
    '''
    A filter which allows levels adjustment on an image.
    
    :param src: (*image*) Source image.
    :param low: (*float*) Low level.
    :param high: (*float*) High level.
    :param low_out: (*float*) Low output level.
    :param high_out: (*float*) High output level.
    
    :returns: Destination image.
    '''
    image = __getimage(src)
    if image is None:
        return None
        
    filter = LevelsFilter()
    filter.setLowLevel(low)
    filter.setHighLevel(high)
    filter.setLowOutputLevel(low_out)
    filter.setHighOutputLevel(high_out)
    dst = BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB)
    filter.filter(image, dst)
    return __getreturn(src, dst)
    
def mask(src, mask=None):
    '''
    Applies a bit mask to each ARGB pixel of an image. You can use this for, say, masking out 
    the red channel.
    
    :param src: (*image*) Source image.
    :param mask: (*int*) Mask color value.
    
    :returns: Destination image.
    '''
    image = __getimage(src)
    if image is None:
        return None
        
    filter = MaskFilter()
    if not mask is None:
        filter.setMask(mask)
    dst = BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB)
    filter.filter(image, dst)
    return __getreturn(src, dst)
    
def posterize(src, n=None):
    '''
    A filter to posterize an image.
    
    :param src: (*image*) Source image.
    :param n: (*int*) Number levels.
    
    :returns: Destination image.
    '''
    image = __getimage(src)
    if image is None:
        return None
        
    filter = PosterizeFilter()
    if not n is None:
        filter.setNumLevels(n)
    dst = BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB)
    filter.filter(image, dst)
    return __getreturn(src, dst)
    
def rescale(src, scale=1):
    '''
    A filter which simply multiplies pixel values by a given scale factor.
    
    :param src: (*image*) Source image.
    :param scale: (*float*) Scale factor.
    
    :returns: Destination image.
    '''
    image = __getimage(src)
    if image is None:
        return None
        
    filter = RescaleFilter()
    filter.setScale(scale)
    dst = BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB)
    filter.filter(image, dst)
    return __getreturn(src, dst)
    
def solarize(src):
    '''
    A filter which solarizes an image.
    
    :param src: (*image*) Source image.
    
    :returns: Destination image.
    '''
    image = __getimage(src)
    if image is None:
        return None
            
    filter = SolarizeFilter()
    dst = BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB)
    filter.filter(image, dst)
    return __getreturn(src, dst)
    
def threshold(src, t=None, lt=127, ut=127, white=None, black=None):
    '''
    A filter which performs a threshold operation on an image.
    
    :param src: (*image*) Source image.
    :param t: (*float*) Threshold.
    :param lt: (*float*) Lower threshold.
    :param ut: (*float*) Upper threshold.
    :param white: (*int*) The color to be used for pixels above the upper threshold.
    :param black: (*int*) The color to be used for pixels blow the lower threshold.
    
    :returns: Destination image.
    '''
    image = __getimage(src)
    if image is None:
        return None
            
    if t is None:
        filter = ThresholdFilter()
        filter.setLowerThreshold(lt)
        filter.setUpperThreshold(ut)
    else:
        filter = ThresholdFilter(t)
    if not white is None:
        filter.setWhite(white)
    if not black is None:
        filter.setBlack(black)
    dst = BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB)
    filter.filter(image, dst)
    return __getreturn(src, dst)
    
def tritone(src, shadow=None, mid=None, high=None):
    '''
    A filter which performs a tritone conversion on an image. Given three colors
    for shadows, midtones and highlights, it converts the image to grayscale and
    then applies a color mapping based on the colors.
    
    :param src: (*image*) Source image.
    :param shadow: (*int*) Shadow color.
    :param mid: (*int*) Midtone color.
    :param high: (*int*) Highlight color
    
    :returns: Destination image.
    '''
    image = __getimage(src)
    if image is None:
        return None
            
    filter = TritoneFilter()
    if not shadow is None:
        filter.setShadowColor(shadow)
    if not mid is None:
        filter.setMidColor(mid)
    if not high is None:
        filter.setHighColor(high)
    dst = BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB)
    filter.filter(image, dst)
    return __getreturn(src, dst)
    
def flip(src, operation=1):
    '''
    A filter which flips images or rotates by multiples of 90 degrees.
    
    :param src: (*image*) Source image.
    :param operation: (*int*) Operation. 1: Flip the image horizontally; 2: Flip the image 
        vertically; 3: Flip the image horizontally and vertically; 4: Rotate the image 90 
        degrees clockwise; 5: Rotate the image 90 degrees counter-clockwise; 6: Rotate the 
        image 180 degrees.
    
    :returns: Destination image.
    '''
    image = __getimage(src)
    if image is None:
        return None
        
    filter = FlipFilter(operation)
    dst = BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB)
    filter.filter(image, dst)
    return __getreturn(src, dst)
    
def rotate(src, angle=0, resize=True):
    '''
    A filter which rotates an image.
    
    :param src: (*image*) Source image.
    :param angle: (*float*) Rotate angle.
    :param resize: (*boolean*) Resize the image or not.
    
    :returns: Destination image.
    '''
    image = __getimage(src)
    if image is None:
        return None
        
    filter = RotateFilter(angle, resize)
    dst = BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB)
    filter.filter(image, dst)
    return __getreturn(src, dst)
    
def emboss(src, azimuth=135, elevation=30, emboss=False, bh=1):
    '''
    This filter will emboss an image. 
    
    :param src: (*image*) Source image.
    :param azimuth: (*float*) Azimuth of the light source.
    :param elevation: (*float*) Elevation of the light source.
    :param emboss: (*boolean*) Emboss or not.
    :param bh: (*float*) Bump height.
    
    :returns: Destination image.
    '''
    image = __getimage(src)
    if image is None:
        return None
        
    filter = EmbossFilter()
    azimuth = math.radians(azimuth)
    elevation = math.radians(elevation)
    filter.setAzimuth(azimuth)
    filter.setElevation(elevation)
    filter.setEmboss(emboss)
    filter.setBumpHeight(bh)
    dst = BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB)
    filter.filter(image, dst)
    return __getreturn(src, dst)
    
def light(src, height=None, shape=None, softness=None, source=None):
    '''
    A filter which produces lighting and embossing effects.
    
    :param src: (*image*) Source image.
    :param height: (*float*) Bump height.
    :param shape: (*int*) Bump shape.
    :param softness: (*float*) Bump softness.
    :param source: (*int*) Bump source.
    
    :returns: Destination image.
    '''
    image = __getimage(src)
    if image is None:
        return None
        
    filter = LightFilter()
    if not height is None:
        filter.setBumpHeight(height)
    if not shape is None:
        filter.setBumpShape(shape)
    if not softness is None:
        filter.setBumpSoftness(softness)
    if not source is None:
        filter.setBumpSource(source)
    dst = BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB)
    filter.filter(image, dst)
    return __getreturn(src, dst)
    
def opacity(src, opacity=None):
    '''
    Sets the opacity (alpha) of every pixel in an image to a constant value.
    
    :param src: (*image*) Source image.
    :param opacity: (*int*) Opacity value (0-255).
    
    :returns: Destination image.
    '''
    image = __getimage(src)
    if image is None:
        return None
        
    filter = OpacityFilter()
    if not opacity is None:
        filter.setOpacity(opacity)

    dst = BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB)
    filter.filter(image, dst)
    return __getreturn(src, dst)
    
def count(a, size):
    '''
    Count none-zero points with window size
    
    :param a: (*array_like*) 2-D array.
    :param size: (*int*) Window size.
    
    :returns: (*array_like*) Count result.
    '''
    r = ImageUtil.count(a.asarray(), size)
    return MIArray(r)
    
def mean(a, size, positive=True):
    '''
    Calculate mean value with window size
    
    :param a: (*array_like*) 2-D array.
    :param size: (*int*) Window size.
    :param positive: (*boolean*) Only calculate the positive value or not.
    
    :returns: (*array_like*) Mean result.
    '''
    r = ImageUtil.mean(a.asarray(), size, positive)
    return MIArray(r)