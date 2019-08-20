// Create an empty text file
import qupath.lib.gui.scripting.QPEx

// NOTE: will save the file in the same folder as original image
current_svs_path = QPEx.getCurrentServerPath()
ann_save_path = current_svs_path.replace('.svs', '.txt')
ann_save_path = ann_save_path.replace('file:', '')

//def roi_types_list = ['Normal', 'Core'] // manually add all the class want to extract here
def roi_types_list = ['Ignore'] // manually add all the class want to extract here
def file = new File(ann_save_path)

file.text = ''
file << "{" << System.lineSeparator() 
// Loop through all objects & write the points to the file
tma_objects_list = QPEx.getAllObjects()
for (roi_type_idx = 0; roi_type_idx < roi_types_list.size(); ++roi_type_idx) {
    roi_type = roi_types_list[roi_type_idx]
    file << '"' << roi_type << '":' << '{'
    file << System.lineSeparator()
    
    track_object_counter = 0
    for (path_object in tma_objects_list) {
        // Check for interrupt (Run -> Kill running script)
        if (Thread.interrupted())
            break
        // Get the ROI
        object_roi = path_object.getROI()
        if (object_roi == null)
            continue
        // polling into specific type dictionary
        object_pathclass = path_object.getPathClass()
        if (object_pathclass != null)
            object_roi_type = object_pathclass.getName()
        else
            object_roi_type = null
        if (object_roi_type != roi_type)
            continue
//        if (roi_types_list.contains(object_roi_type))
//            continue
//        if (object_roi.getRoiName() != 'Rectangle')
//            continue
        print object_roi_type
       
        if (track_object_counter != 0)
            file << "," << System.lineSeparator()
        
        // Write the points; but beware of areas, and also ellipses!
        file << '\t'
        if (object_roi_type == 'Core')           
            file << '"' << path_object.getName() << '"'
        else
            file << '"' << 'Object' << track_object_counter << '"'  
        file << " : ["
                
        points_list = object_roi.getPolygonPoints()
        for (point_idx = 0; point_idx < points_list.size(); ++point_idx) {
            point = points_list[point_idx]
            file << System.lineSeparator() << "\t\t"
            file << "{"
            file << '"X":"' << point.getX() << '"'
            file << ","
            file << '"Y":"' << point.getY() << '"'
            file << "}"
            if (point_idx != points_list.size() - 1)
                file << ","
        }
        file << System.lineSeparator() << "\t]"
        track_object_counter += 1        
    }
    file << "}"
    if (roi_type_idx != roi_types_list.size() - 1)
        file << ","
    file << System.lineSeparator()
}
file << "}" << System.lineSeparator() 
print 'Done!'