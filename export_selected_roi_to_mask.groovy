import qupath.lib.images.servers.ImageServer
import qupath.lib.objects.PathObject
import qupath.lib.regions.RegionRequest
import qupath.lib.roi.PathROIToolsAwt
import qupath.lib.scripting.QPEx
import qupath.lib.common.ColorTools

import javax.imageio.ImageIO
import java.awt.Color
import java.awt.image.BufferedImage

// paramters for output
def downsample = 1.0
def pathOutput = 'H:/wsi-tma/BT140001/anns/'
QPEx.mkdirs(pathOutput) // will keep not delete old dir if exists

// Get the main QuPath data structures
def imageData = QPEx.getCurrentImageData()
def hierarchy = imageData.getHierarchy()
def wsi_data = imageData.getServer()
def wsi_name = wsi_data.getShortServerName()

core_object = QPEx.getSelectedObject()
def core_color = core_object.getPathClass().getColor()
core_color = new Color(ColorTools.red(core_color),
                       ColorTools.green(core_color),
                       ColorTools.blue(core_color))
def core_roi = core_object.getROI()
// Create a region from the ROI
def core_region = RegionRequest.createInstance(wsi_data.getPath(), downsample, core_roi)
def core_shape = PathROIToolsAwt.getShape(core_roi)
// Request the BufferedImage
def core_img = wsi_data.readBufferedImage(core_region)
def core_msk = new BufferedImage(core_img.getWidth(), core_img.getHeight(), BufferedImage.TYPE_INT_RGB)
def core_msk_g2d = core_msk.createGraphics() // request drawing interface
// paint the core upon the canvas background
core_msk_g2d.setColor(core_color) // select brush color
core_msk_g2d.translate(-core_region.getX(), -core_region.getY())
core_msk_g2d.fill(core_shape)

for (child_object in core_object.getChildObjects()) {
    child_roi = child_object.getROI()  
    def child_color = child_object.getPathClass().getColor()
    child_color = new Color(ColorTools.red(child_color),
                            ColorTools.green(child_color),
                            ColorTools.blue(child_color)) 
    // Create a mask using Java2D functionality
    // (This involves applying a transform to a graphics object, 
    // so that none needs to be applied to the ROI coordinates)
    def child_shape = PathROIToolsAwt.getShape(child_roi)
    core_msk_g2d.setColor(child_color) // select brush color
    core_msk_g2d.fill(child_shape)    
}    
core_msk_g2d.dispose()

// Export the mask
def fileMask = new File(pathOutput, core_object.getName() + '.png')
ImageIO.write(core_msk, 'PNG', fileMask)

