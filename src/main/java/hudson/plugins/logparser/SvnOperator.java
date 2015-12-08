package hudson.plugins.logparser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNProperties;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

/**
 * A class that is used to preform common svn functions.
 * Like checkout a repository or a file or see revisions of a file
 *
 * @author Jimmy Chen
 */
public class SvnOperator {
    String svnURL;
    String username;
    String password;
    SVNRepository repository = null;

    /**
     * Creates an instance of the svn operator.
     * Takes in the url of the repository, the username and the password.
     *
     * @param svnURL the svn url
     * @param username the username
     * @param password the password
     */
    public SvnOperator(String svnURL, String username, String password) {
        this.svnURL = svnURL;
        this.username = username;
        this.password = password;
        try {
            repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(svnURL));
        } catch (SVNException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(username, password);
        repository.setAuthenticationManager(authManager);

    }

    /**
     * @return svnURL it returns the url the svnoperator is for.
     */
    public String getSvnURL() {
        return svnURL;
    }

    /**
     * Allows you to change the url of the repository instance
     *
     * @param svnURL the svn url
     */
    public void setSvnURL(String svnURL) {
        this.svnURL = svnURL;
    }

    /**
     * @return username it returns you the username of the svn instance
     */
    public String getUsername() {
        return username;
    }

    /**
     * allows you to change the username of the repository instance
     *
     * @param username the username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * gets the password of the repository instance
     *
     * @return password
     */
    public String getPassword() {
        return password;
    }

    /**
     * allows you change the repository instance's password
     *
     * @param password the password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * checks out the repository at the given filepath. Throws an exception if the path doesn't work out
     *
     * @param filePath the destination filepath
     * @throws Exception
     */
    public void checkoutRepository(String filePath) throws Exception {
        if (repository == null) {
            try {
                repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(svnURL));
            } catch (SVNException e) {
                e.printStackTrace();
            }
        }
        //create authentication data
        ISVNAuthenticationManager authManager = SVNWCUtil
                .createDefaultAuthenticationManager(username, password);
        repository.setAuthenticationManager(authManager);

        // output some data to verify connection
        //System.out.println("Repository Root: "
        //	+ repository.getRepositoryRoot(true));
        //System.out.println("Repository UUID: "
        //+ repository.getRepositoryUUID(true));

        // need to identify latest revision
        long latestRevision = repository.getLatestRevision();
        //System.out.println("Repository Latest Revision: " + latestRevision);

        // create client manager and set authentication
        SVNClientManager ourClientManager = SVNClientManager.newInstance();
        ourClientManager.setAuthenticationManager(authManager);

        // use SVNUpdateClient to do the export
        SVNUpdateClient updateClient = ourClientManager.getUpdateClient();
        updateClient.setIgnoreExternals(false);

        updateClient.doExport(repository.getLocation(), new File(filePath),
                SVNRevision.create(latestRevision),
                SVNRevision.create(latestRevision), null, true,
                SVNDepth.INFINITY);
    }

    /**
     * Checkouts the latest version of the file from the repository if it exists.
     * If it doesn't exist it returns null.
     *
     * @param filePath the destination filepath
     * @param fileName the file name
     * @return File the file you wanted to checkout
     * @throws Exception if the filepath doesn't work out
     */
    public File getFileFromCheckout(String filePath, String fileName) throws Exception {
        File svnDir = new File(filePath);
        if (!svnDir.isDirectory()) {
            checkoutRepository(filePath);
        }
        Collection files = FileUtils.listFiles(svnDir, null, true);
        for (Iterator iterator = files.iterator(); iterator.hasNext(); ) {
            File file = (File) iterator.next();
            if (file.getName().equals(fileName)) {
                return file;
            }
        }
        return null;
    }

    /**
     * checks out a particular revision of the repository to the filepath specified.
     *
     * @param filePath the filepath
     * @param revisionNumber the revision number
     * @throws SVNException if the path doesn't work, or svn has issues with the specifics
     */
    public void checkoutRepositoryRevision(String filePath, long revisionNumber) throws SVNException {
        if (repository == null) {
            try {
                repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(svnURL));
            } catch (SVNException e) {
                e.printStackTrace();
            }
        }
        //create authentication data
        ISVNAuthenticationManager authManager = SVNWCUtil
                .createDefaultAuthenticationManager(username, password);
        repository.setAuthenticationManager(authManager);

        // output some data to verify connection
        //System.out.println("Repository Root: "
        //	+ repository.getRepositoryRoot(true));
        //System.out.println("Repository UUID: "
        //+ repository.getRepositoryUUID(true));

        // need to identify latest revision
        //System.out.println("Repository Latest Revision: " + latestRevision);

        // create client manager and set authentication
        SVNClientManager ourClientManager = SVNClientManager.newInstance();
        ourClientManager.setAuthenticationManager(authManager);

        // use SVNUpdateClient to do the export
        SVNUpdateClient updateClient = ourClientManager.getUpdateClient();
        updateClient.setIgnoreExternals(false);

        updateClient.doExport(repository.getLocation(), new File(filePath),
                SVNRevision.create(revisionNumber),
                SVNRevision.create(revisionNumber), null, true,
                SVNDepth.INFINITY);

    }

    /**
     * Gives a list of the revision numbers of the file. The significant changes.
     *
     * @param file the file name
     * @return array list of the revision numbers, empty if the file doesn't exist
     * @throws Exception if svn fails
     */
    public ArrayList<Long> showRevisionNumbersOfFile(String file) throws Exception {
        if (repository == null) {
            try {
                repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(svnURL));
            } catch (SVNException e) {
                e.printStackTrace();
            }
        }
        ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(username, password);
        repository.setAuthenticationManager(authManager);
        SVNNodeKind nodeKind = repository.checkPath(file, -1);
        if (nodeKind == SVNNodeKind.NONE) {
            throw new Exception("file does not exist");
        } else {
            ArrayList<Long> revisions = null;
            repository.getFileRevisions(file, revisions, 0, -1);
            return revisions;
        }
    }

    /**
     * Returns the output stream of the file from the repository if it exists most recent version.
     *
     * @param file the file name
     * @return output stream of a file
     * @throws Exception if the file does not exist
     */
    public OutputStream getOutputStreamFromRepository(String file) throws Exception {
        if (repository == null) {
            try {
                repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(svnURL));
            } catch (SVNException e) {
                e.printStackTrace();
            }
        }
        ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(username, password);
        repository.setAuthenticationManager(authManager);
        SVNNodeKind nodeKind = repository.checkPath(file, -1);
        if (nodeKind == SVNNodeKind.NONE) {
            throw new Exception("file does not exist");
        } else if (nodeKind == SVNNodeKind.DIR)
            throw new Exception("file a directory not a file");
        else {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            SVNProperties fileProps = null;
            repository.getFile(file, -1, fileProps, baos);
            return baos;
        }
    }

    /**
     * Returns the output stream of the file from the repository if it exists most recent version. Version specified
     *
     * @param file the file name
     * @param revisionNumber the revision number
     * @return output stream of a file
     * @throws Exception if file does not exist
     */
    public OutputStream getOutputStreamFromRepositoryRevision(String file, long revisionNumber) throws Exception {
        if (repository == null) {
            try {
                repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(svnURL));
            } catch (SVNException e) {
                e.printStackTrace();
            }
        }
        ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(username, password);
        repository.setAuthenticationManager(authManager);
        SVNNodeKind nodeKind = repository.checkPath(file, -1);
        if (nodeKind == SVNNodeKind.NONE) {
            throw new Exception("file does not exist");
        } else if (nodeKind == SVNNodeKind.DIR)
            throw new Exception("file a directory not a file");
        else {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            SVNProperties fileProps = null;
            repository.getFile(file, revisionNumber, fileProps, baos);
            return baos;
        }
    }
}
