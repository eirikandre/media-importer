# media-importer

Media-importer is made to import and organize pictures after date by reading the EXIF information in the image.
 
It will order the files in folders by year/month/day. If the file name already exists, it will create a hash of each
file to verify if it is the same file. If it is, it will not copy. If the file is not the same, it
will create a new folder called "conflict" that you need to resolve manually. It  will report about 
all files in the end of the process.

# How to run
````
java -jar C:\apps\media-importer.jar <action> <from> <to>
````

Possible actions:
* dry (Will only analyze and sprint out what it want to do)
* copy
* move (Will not work to move files from one disk to other disk) 

# Supported file types:
|File type| comment|
|---------------|-----------|
|NEF, RAF, JPEG, JPG, TIFF, PNG, AAE | Reading date from ExifSubIFDDirectory | 
|MOV, M4V, MP4| Reading date from last modified time |
