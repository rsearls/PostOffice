package org.jboss.resteasy.test.resource.basic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericEntity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataOutput;
import org.jboss.resteasy.spi.HttpResponseCodes;
import org.jboss.resteasy.test.resource.basic.resource.PostOfficeResource;
import org.jboss.resteasy.utils.PortProviderUtil;
import org.jboss.resteasy.utils.TestUtil;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.impl.base.exporter.zip.ZipExporterImpl;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
@RunAsClient
public class PostOfficeTest {

    private static Client client;

    @Deployment
    public static Archive<?> deploy() {
        WebArchive war = TestUtil.prepareArchive(PostOfficeTest.class.getSimpleName())
                .addAsWebInfResource(PostOfficeTest.class.getPackage(), "postofficeJbossWeb.xml", "jboss-web.xml");
        return TestUtil.finishContainerPrepare(war, null, PostOfficeResource.class);
    }

    @BeforeClass
    public static void init() {
        // writeArchiveToDisk();
        client = ClientBuilder.newClient();
    }

    @AfterClass
    public static void after() throws Exception {
        client.close();
        client = null;
    }

    private static void writeArchiveToDisk(){
        WebArchive war = (WebArchive) deploy();
        ZipExporterImpl zipExporter = new ZipExporterImpl(war);
        // env var is declared in the pom.xml
        String s =  System.getenv("test.war.output.dir");
        String fileName = s + File.separator + war.getName();
        try (InputStream inputStream = zipExporter.exportAsInputStream();)
        {
            try (FileOutputStream outputStream = new FileOutputStream(fileName))
            {
                inputStream.transferTo(outputStream);
            }
        } catch (IOException e) {
            assertTrue(e.getMessage(), false);
        }
    }

    private String generateURL(String path) {
        // context-root set to cloud in jboss-web.xml
        return PortProviderUtil.generateURL(path, "cloud");
    }

    @Test
    public void ping() {
        WebTarget base = client.target(generateURL("/postoffice/ping"));
        try {
            Response response = base.request().get();
            Assert.assertEquals(HttpResponseCodes.SC_OK, response.getStatus());
            String body = response.readEntity(String.class);
            Assert.assertTrue(body.startsWith("pong"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    /**
     * Upload a file.
     * Confirm the upload
     */
    public void uploadWar() throws Exception {
        WebArchive war = (WebArchive) deploy();
        ZipExporterImpl zipExporter = new ZipExporterImpl(war);
        WebTarget target = client.target(generateURL("/postoffice/upload"));

        MultipartFormDataOutput mdo = new MultipartFormDataOutput();

        try (InputStream inputStream = zipExporter.exportAsInputStream();) {
            mdo.addFormData("attachment", inputStream,
                    MediaType.APPLICATION_OCTET_STREAM_TYPE, war.getName());
            GenericEntity<MultipartFormDataOutput> entity = new GenericEntity<MultipartFormDataOutput>(mdo) {
            };
            Response r = target.request().post(
                    Entity.entity(entity, MediaType.MULTIPART_FORM_DATA_TYPE));

            // Check that upload was successful
            assertEquals(200, r.getStatus());

            // check file presence
            target = client.target(generateURL("/postoffice/checkmail"));
            r = target.request().get();
            String body = r.readEntity(String.class);
            Assert.assertTrue( body.contains(war.getName()));
        }
    }

    @Test
    /**
     * Upload a file.
     * Confirm the upload
     * Delte the file
     * Confirm it was deleted
     */
    public void deleteUpload() throws Exception {
        WebArchive war = (WebArchive) deploy();
        ZipExporterImpl zipExporter = new ZipExporterImpl(war);
        WebTarget target = client.target(generateURL("/postoffice/upload"));

        MultipartFormDataOutput mdo = new MultipartFormDataOutput();

        try (InputStream inputStream = zipExporter.exportAsInputStream();) {
            mdo.addFormData("attachment", inputStream,
                    MediaType.APPLICATION_OCTET_STREAM_TYPE, war.getName());
            GenericEntity<MultipartFormDataOutput> entity = new GenericEntity<MultipartFormDataOutput>(mdo) {
            };
            Response r = target.request().post(
                    Entity.entity(entity, MediaType.MULTIPART_FORM_DATA_TYPE));

            // Check that upload was successful
            assertEquals(200, r.getStatus());

            // check file presence
            target = client.target(generateURL("/postoffice/checkmail"));
            r = target.request().get();
            String body = r.readEntity(String.class);
            Assert.assertTrue( body.contains(war.getName()));

            // delete file
            target = client.target(generateURL("/postoffice/delete/" +war.getName()));
            r = target.request().delete();
            String result = r.readEntity(String.class);
            Assert.assertTrue( result.startsWith("success"));

            // check for empty directory
            target = client.target(generateURL("/postoffice/checkmail"));
            r = target.request().get();
            String emptyDir = r.readEntity(String.class);
            Assert.assertTrue( emptyDir.contains("is empty"));
        }
    }
}
