import qupath.lib.scripting.QPEx

// Loop through all objects & write the points to the file
char rowHeader='A';
def objectList = QPEx.getAllObjects()
// 1st object is null
for (int i = 1; i < objectList.length; ++i) {
    if (objectList[i].getROI() == null)
        continue
    colHeader = i % 16 == 0 ? 16 : i % 16
//    print sprintf('%s%02d', rowHeader, colHeader)
    objectList[i].setName(sprintf('%s%02d', rowHeader, colHeader))
    if (i % 16 == 0)
        rowHeader++
}
print 'Done!'