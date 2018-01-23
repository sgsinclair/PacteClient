package ca.crim.nlp.pacte.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import ca.crim.nlp.pacte.QuickConfig;

public class CorpusTest {
    @Rule
    public TemporaryFolder poTestFolder = new TemporaryFolder();

    /**
     * Create the test user
     */
    @Before
    public void checkTestSubject() {
        SampleBuilder.createTestingUser();
    }

    /**
     * Create, populate and delete a corpus.
     */
    @Test
    public void corpusLifeCycle() throws InterruptedException {
        String lsNewCorpusName = UUID.randomUUID().toString() + UUID.randomUUID().toString();
        String lsCorpusID = null;
        String lsReturn = null;
        String lsDociID = null;
        String lsGroupID = null;

        Corpus loCorpus = new Corpus(new QuickConfig());

        // Create the corpus
        System.out.print("Creating new corpus... ");
        lsReturn = loCorpus.createCorpus(lsNewCorpusName, "fr_fr");
        if (lsReturn != null && !lsReturn.isEmpty())
            lsCorpusID = lsReturn;
        assertNotNull(lsCorpusID);
        System.out.println("Created!");

        // Populate
        System.out.print("Adding document... ");
        lsDociID = loCorpus.addDocument(lsCorpusID, "bla bla bla", "bla", "none", "fr_fr");
        assertNotNull(lsDociID);
        System.out.println("Added!");

        // Create annotation group
        System.out.print("Creating annotation group... ");
        lsGroupID = loCorpus.createBucket(lsCorpusID, UUID.randomUUID().toString());
        assertNotNull(lsGroupID);
        System.out.println("Created!");

        // TODO: Removing group

        // Remove document
        Thread.sleep(500); //
        System.out.print("Deleting document... ");
        assertTrue(loCorpus.getDocument(lsCorpusID, lsDociID).getTitle().equals("bla"));
        System.out.println("Deleted!");

        // Delete
        System.out.print("Deleting corpus...");
        assertTrue(loCorpus.deleteCorpus(lsCorpusID));
        lsReturn = loCorpus.getCorpusId(lsNewCorpusName);
        assertNull(lsReturn);
        assertNull(loCorpus.getCorpusId(lsNewCorpusName));
        System.out.println("Deleted!");
        System.out.println("Done!");
    }

    /**
     * Export the sample corpus in a temporary folder.
     * 
     * @return The path for the exported corpus
     * @throws IOException
     */
    @Test
    public void testExportCorpus() throws IOException {
        String lsCorpusId = null;
        String lsExportPath = null;
        Corpus loCorpus = new Corpus(new QuickConfig());

        lsCorpusId = SampleBuilder.smallCorpus(loCorpus);
        assertNotNull(lsCorpusId);

        lsExportPath = exportCorpus(loCorpus, lsCorpusId);
        System.out.println(lsExportPath);

        // Stuff exported?
        assertNotNull(lsExportPath);
        assertTrue(new File(lsExportPath).list().length > 0);

        // Only four groups?

        // All documents exported?
        assertEquals(2, new File(lsExportPath, "documents").list().length);

    }

    private String exportCorpus(Corpus toCorpus, String tsCorpusId) {
        List<String> lasGroupList = new ArrayList<String>();
        File loExportPath = null;

        try {
            loExportPath = poTestFolder.newFolder();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return toCorpus.exportToDisk(tsCorpusId, loExportPath.getAbsolutePath(), lasGroupList)
                ? loExportPath.getAbsolutePath() : null;
    }

    @Test
    public void testImportCorpus() throws IOException, InterruptedException {
        String lsCorpusId = null;
        Corpus loCorpus = new Corpus(new QuickConfig());
        String lsSourcePath = null;

        // Export the corpus before running the test
        lsCorpusId = SampleBuilder.smallCorpus(loCorpus);
        lsSourcePath = exportCorpus(loCorpus, lsCorpusId);

        assertNotNull(lsSourcePath);
        assertTrue(new File(lsSourcePath).exists());

        // The real test
        lsCorpusId = loCorpus.importCorpus(lsSourcePath);

        assertNotNull(lsCorpusId);
        assertNotNull(loCorpus.getCorpusMetadata(lsCorpusId));
        Thread.sleep(1000); //  
        assertTrue(loCorpus.getSize(lsCorpusId) >= 2);

        // Delete imported corpus after successful test
        loCorpus.deleteCorpus(lsCorpusId);
    }
}
