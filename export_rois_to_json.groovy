// Create an empty text file
import qupath.lib.scripting.QPEx
import qupath.lib.roi.*
import com.google.gson.GsonBuilder

// a script for exporting ROIs as vertices and other metadata in JSON format
// to be replaced with proper serialization of qupath.lib.roi.AreaROI
def adaptPolygon(roi) {
    def vert = []
    for (point in roi.getPolygonPoints()){
        vert.add([point.x, point.y]);
    }
    return vert
}

def adaptPathObject(obj){
    obj_roi = obj.getROI()
    obj_type = obj.getPathClass()
    // polling into specific type dictionary
    obj_type = obj_type != null ? obj_type.getName() : null

    def obj_info = [:]    
    obj_info['Type'] = obj_type
    obj_info['Name'] = obj.getName()
    if (!obj_roi.getROIType().contains('Area')) {
        obj_info["Area"]     = false
        obj_info["Vertices"] = adaptPolygon(obj_roi)
    } else {
        // [1] is convex hulls (exterior), 
        // [0] represents the holes (interior)
        polygons = PathROIToolsAwt.splitAreaToPolygons(obj_roi)
        def sub_ext_jroi = []
        for (sub_roi in polygons[1]) {
            sub_ext_jroi.add(adaptPolygon(sub_roi))
        }

        def sub_int_jroi = []
        for (sub_roi in polygons[0]) {
            sub_int_jroi.add(adaptPolygon(sub_roi))
        }
        obj_info["Area"] = true
        obj_info["Exterior"] = sub_ext_jroi
        obj_info["Interior"] = sub_int_jroi
    }
    return obj_info
}

// NOTE: will save the file in the same folder as original image
current_svs_path = QPEx.getCurrentServerPath()
ann_save_path = current_svs_path.replace('.svs', '.txt')
ann_save_path = ann_save_path.replace('file:', '')

// manually add all the annotation class want to extract here  
//def pathclass_list = ['RoI', 'RoI_Viz_Val', 'Epithelium', 'Ignore']
def pathclass_list = ['RoI']

ann_info = []
// Loop through all objects & write the points to the file
object_list = QPEx.getAllObjects()
for (object in object_list) {
    // Check for interrupt (Run -> Kill running script)
    if (Thread.interrupted()) break
    object_roi = object.getROI()
    object_pathclass = object.getPathClass()
    if (object_roi == null) continue
    // polling into specific type dictionary
    object_pathclass = object_pathclass != null ? object_pathclass.getName() : null
    print "$object_pathclass, $object_roi"
    if (!pathclass_list.contains(object_pathclass)) continue
    ann_info.add(adaptPathObject(object))
}
gson = new GsonBuilder().setPrettyPrinting().create()

def file = new File(ann_save_path)
file.newWriter().withWriter { w ->
  w << gson.toJson(ann_info)
}
print 'Done!'