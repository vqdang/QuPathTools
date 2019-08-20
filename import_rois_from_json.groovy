// Create an empty text file
import qupath.lib.scripting.QPEx
import qupath.lib.roi.*

import qupath.lib.geom.Point2
import qupath.lib.objects.PathAnnotationObject
import qupath.lib.roi.PolygonROI

import com.google.gson.Gson
import groovy.json.JsonSlurper

// file_path = '/home/vqdang/work/tissue-new/epi-str_wsi/data.json'
file_path = '/home/vqdang/work/tissue-new/epi-str_wsi/output/TMA/OutcomesTMABlock4_HE_slide37.json'

def file_string = new File(file_path).text
// depend on the formar of string, may ned swap List to Map in other case
def json_file = new Gson().fromJson(file_string, List)

// Only for no-hole for now
// Convert to QuPath annotations
// def annotation_object_list = []
// ! // For list of list
// for (roi in json_file) {    
//     def annotation // similar to C-scope so inner cant go out
//     if (roi.size() == 4){
//         // ! un-optimized
//         def min_x = roi.collect{it[0]}.min()
//         def max_x = roi.collect{it[0]}.max()
//         def min_y = roi.collect{it[1]}.min()
//         def max_y = roi.collect{it[1]}.max()
//         annotation = new RectangleROI(min_x, min_y, 
//                                 max_x-min_x, max_y-min_y)
//     }
//     else {
//         def vertices = roi.collect {new Point2(it[0], it[1])}
//         annotation = new PolygonROI(vertices)                       
//     }
//     def pathAnnotation = new PathAnnotationObject(annotation)
//     annotation_object_list << pathAnnotation
// }

// ! For list of dict
def annotation_object_list = []
for (roi in json_file) {    
    def annotation // similar to C-scope so inner cant go out
    roi_type = roi['Type'] // may check if exist or dict or sthg

    roi_vertices = roi['Vertices']
    //print(roi_vertices)
    if (roi_vertices.size() < 3) continue
    if (roi_vertices.size() == 4){
        // ! un-optimized
        def min_x = roi_vertices.collect{it[0]}.min()
        def max_x = roi_vertices.collect{it[0]}.max()
        def min_y = roi_vertices.collect{it[1]}.min()
        def max_y = roi_vertices.collect{it[1]}.max()
        annotation = new RectangleROI(min_x, min_y, 
                                max_x-min_x, max_y-min_y)
    }
    else {
        def qupath_vertices = roi_vertices.collect {new Point2(it[0], it[1])}
        annotation = new PolygonROI(qupath_vertices)                       
    }
    def pathAnnotation = new PathAnnotationObject(annotation)
    pathAnnotation.setPathClass(QPEx.getPathClass(roi_type))
    annotation_object_list << pathAnnotation
}

// Add to current hierarchy
QPEx.addObjects(annotation_object_list)