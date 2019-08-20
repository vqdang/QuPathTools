import qupath.lib.scripting.QPEx

def hierarchy = QPEx.getCurrentHierarchy()
def objectList = QPEx.getAllObjects()
objectList = objectList.sort {it.getName()}
// 1st object is null
for (int i = 1; i < objectList.length; ++i) {
    hierarchy.removeObject(objectList[i], false)
}
// 1st object is null
for (int i = 1; i < objectList.length; ++i) {
    if (objectList[i].getROI() == null)
        continue
    hierarchy.addPathObject(objectList[i], false)
}
print 'Done!'