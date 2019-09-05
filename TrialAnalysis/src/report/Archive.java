package report;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import org.apache.log4j.Logger;

import utils.Funcs;
import utils.Globals;
import data.xml.objects.Trial;

/**
 * Create archive folder with timestamp and move all files from a folder into
 * the archive.
 * 
 * @author Scott Smith
 *
 */
public class Archive {

	private static Logger log = Logger.getLogger(Archive.class);
	
	/**
	 * Archive a trial, consisting of archiving both report and working folders
	 * @param trial
	 * @param exclusion - List of files at should only be copied into the archive leaving the original file untouched.
	 */
	public static void archive(Trial trial, Collection<String> exclusion){
		Path destinationDirectory = Paths.get(trial.getTrialDirectory(), getBaseFolderName());
		Path sourceDirectory = Paths.get(trial.getTrialDirectory());
		
		try{
			BasicFileAttributes attr = Files.readAttributes(Paths.get(trial.getTrialWorkDirectory()), BasicFileAttributes.class);
			FileTime timeStamp = attr.creationTime();
			Funcs.createWithPermissions(destinationDirectory, sourceDirectory, true);
			archive(destinationDirectory, sourceDirectory, timeStamp, exclusion);
		}
		catch(Exception e){
			log.error(e);
		}
	}
	
	public static String getBaseFolderName(){
		return Globals.ARCHIVE_FOLDER_PREFIX.toLowerCase().substring(0, Globals.ARCHIVE_FOLDER_PREFIX.length()-1);
	}
	
	/**
	 * Take directory and archive the contents to a timestamped folder.
	 * 
	 * @param destinationDirectory - location to place the zip'd file.
	 * @param sourceDirectory - folder to archive
	 * @param exclusion
	 */
	public static void archive(Path destinationDirectory, Path sourceDirectory, FileTime timeStamp, Collection<String> exclusion) {

		Path baseDirectory = sourceDirectory;

		try {
			Files.walkFileTree(baseDirectory, new MoveFileVisitor(exclusion, destinationDirectory, timeStamp));
			
			//delete if archive folder is empty
			Path archivePath = destinationDirectory.resolve(createArchiveFolderName(timeStamp));
			try(DirectoryStream<Path> ds = Files.newDirectoryStream(archivePath)){
				if(!ds.iterator().hasNext()){
					Files.delete(archivePath);
				}
			}
		}
		catch (Exception e) {
			log.error("Moving file to archive - " + baseDirectory, e);
		}
	}

	private static String createArchiveFolderName(FileTime timestamp){
		DateFormat dateFormater = new SimpleDateFormat(Globals.ARCHIVE_DATE_FORMAT);
		return Globals.ARCHIVE_FOLDER_PREFIX + dateFormater.format(new Date(timestamp.toMillis()));
	}
	
	/**
	 * Create folder with the name ARCHIVE_ and the timestamp
	 * 
	 * @param dir - Directory where the archive folder will be placed
	 * @param timestamp - Timstamp appended to the archive folders name.
	 * @return - Path of the newly created folder
	 * @throws IOException
	 */
	public static Path createArchiveFolder(Path dir, FileTime timestamp) throws IOException{
		String archiveFolderName = createArchiveFolderName(timestamp);
		Path archiveFolder = dir.resolve(archiveFolderName);
		
		//create archive folder
		try {
			Funcs.createWithPermissions(archiveFolder,true);
		} 
		catch (IOException e) {
			log.error("Creating archive directory - " + dir);
			throw e;
		}
		
		return archiveFolder;
	}
}
	class MoveFileVisitor extends SimpleFileVisitor<Path> {
	    private Path targetPath;
	    private Path source;
	    private boolean create;
	    FileTime timeStamp;
	    Collection<String> exclusion;
	    
	    public MoveFileVisitor(Collection<String> exclusion, Path targetPath, FileTime timeStamp) {
	        this.exclusion= exclusion;
	        this.targetPath = targetPath;
	        this.timeStamp = timeStamp;
	        create = true;
	    }

	    /**
	     * If there's nothing archive anything return true.
	     * @param dir
	     * @return
	     * @throws IOException
	     */
	    private boolean needToArchive(Path dir) throws IOException{
	    	boolean result = false;
	    	try(DirectoryStream<Path> ds = Files.newDirectoryStream(dir)){
		    	for(Path file : ds){
		    		if(!file.toString().contains(Archive.getBaseFolderName())){
		    			result = true;
		    			break;
		    		}
		    	}
	    	}
			return result;
	    }
	    
	    @Override
	    public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
	    	FileVisitResult result = FileVisitResult.CONTINUE;
	    	
	    	//skip over the archive folder.
    		if(!needToArchive(dir)){
    			return FileVisitResult.SKIP_SUBTREE;
    		}
    		
	    	if(create){
	    		targetPath = Archive.createArchiveFolder(targetPath, timeStamp);
	    		create = !create;
	        }

	    	if(source == null){
	    		source = dir;
	    	}
	        
	        //don't create folder for the base folder as it's already the archive
	        if(!source.equals(dir)){
	        	Path path = targetPath.resolve(source.relativize(dir));
	        	Funcs.createWithPermissions(path,true);
	        }

	        return result;
	    }

	    @Override
	    public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
//	    	if(create){
//	        	targetPath = Archive.createArchiveFolder(file.getParent(), new Date(attrs.creationTime().toMillis()));
//	        	create = !create;
//	        }
	    	
	    	//skip archive folders
	    	if(!file.getParent().toString().contains(Globals.ARCHIVE_FOLDER_PREFIX)){
	    		//if file is on the exclusion list, copy don't move.
	    		if(exclusion.contains(file.getFileName().toString())){
	    			Files.copy(file, targetPath.resolve(source.relativize(file)), StandardCopyOption.REPLACE_EXISTING);
	    		}
	    		else{
	    			Files.move(file, targetPath.resolve(source.relativize(file)), StandardCopyOption.REPLACE_EXISTING);
	    		}
	    	}
	    	return FileVisitResult.CONTINUE;
	    }
	}