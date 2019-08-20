import qupath.lib.scripting.QP
import qupath.lib.geom.Point2
import qupath.lib.roi.PolygonROI
import qupath.lib.objects.PathAnnotationObject
import qupath.lib.images.servers.ImageServer

//Aperio Image Scope displays images in a different orientation
def rotated = true

def server = QP.getCurrentImageData().getServer()
def h = server.getHeight()
def w = server.getWidth()

// need to add annotations to hierarchy so qupath sees them
def hierarchy = QP.getCurrentHierarchy()

//Prompt user for exported aperio image scope annotation file
//def file = QP.getQuPath()//.getDialogHelper().promptForFile('xml', null, 'aperio xml file', null)
//def text = file.getText()
def text = new File('/media/vqdang/Data/Workspace/KBSMC/PROSTATE/11S-1_2(x400).xml').getText()

def list = new XmlSlurper().parseText(text)

def ann_types = [
        'benign': 'normal', 
        '3': 'Grade 3', 
        '4': 'Grade 4', 
        '5': 'Grade 5'
] // manually add all the class want to extract here

for (ann_xml in list.Annotation) {
    if (!(ann_xml.@Name in ann_types.keySet()))
        continue
    ann_path_class = QP.getPathClass(ann_types[ann_xml.@Name])
    for (region_xml in ann_xml.Regions.Region) {
        def tmp_points_list = []
        for (vertex_xml in region_xml.Vertices.Vertex) {
            if (!rotated) {
                X = vertex_xml.@Y.toDouble()
                Y = h - vertex_xml.@X.toDouble()
            }
            else {
                X = vertex_xml.@X.toDouble()
                Y = vertex_xml.@Y.toDouble()
            }
            tmp_points_list.add(new Point2(X, Y))
        }
        def roi = new PolygonROI(tmp_points_list)
        def annotation = new PathAnnotationObject(roi)
        annotation.setPathClass(ann_path_class)
        hierarchy.addPathObject(annotation, false)
    }
    break
}