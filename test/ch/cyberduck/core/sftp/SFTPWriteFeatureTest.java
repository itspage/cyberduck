package ch.cyberduck.core.sftp;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DefaultHostKeyController;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginController;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class SFTPWriteFeatureTest extends AbstractTestCase {

    @Test
    public void testWrite() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", new Credentials(
                properties.getProperty("sftp.user"), properties.getProperty("sftp.password")
        ));
        final SFTPSession session = new SFTPSession(host);
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final TransferStatus status = new TransferStatus();
        final byte[] content = "test".getBytes("UTF-8");
        status.setLength(content.length);
        final Path test = new Path(session.workdir(), UUID.randomUUID().toString(), Path.FILE_TYPE);
        final OutputStream out = new SFTPWriteFeature(session).write(test, status);
        assertNotNull(out);
        IOUtils.write(content, out);
        IOUtils.closeQuietly(out);
        assertTrue(new SFTPFindFeature(session).find(test));
        assertEquals(content.length, session.list(test.getParent(), new DisabledListProgressListener()).get(test.getReference()).attributes().getSize());
        assertEquals(content.length, new SFTPWriteFeature(session).append(test, status, Cache.empty()).size, 0L);
        {
            final byte[] buffer = new byte[content.length];
            final InputStream in = new SFTPReadFeature(session).read(test, new TransferStatus());
            IOUtils.readFully(in, buffer);
            IOUtils.closeQuietly(in);
            assertArrayEquals(content, buffer);
        }
        {
            final byte[] buffer = new byte[content.length - 1];
            final InputStream in = new SFTPReadFeature(session).read(test, new TransferStatus().append(true).current(1L));
            IOUtils.readFully(in, buffer);
            IOUtils.closeQuietly(in);
            final byte[] reference = new byte[content.length - 1];
            System.arraycopy(content, 1, reference, 0, content.length - 1);
            assertArrayEquals(reference, buffer);
        }
        new SFTPDeleteFeature(session).delete(Collections.<Path>singletonList(test), new DisabledLoginController());
        session.close();
    }

    @Test
    public void testWriteSymlink() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", new Credentials(
                properties.getProperty("sftp.user"), properties.getProperty("sftp.password")
        ));
        final SFTPSession session = new SFTPSession(host);
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final Path target = new Path(session.workdir(), UUID.randomUUID().toString(), Path.FILE_TYPE);
        new SFTPTouchFeature(session).touch(target);
        assertTrue(new SFTPFindFeature(session).find(target));
        final String name = UUID.randomUUID().toString();
        final Path symlink = new Path(session.workdir(), name, Path.FILE_TYPE | Path.SYMBOLIC_LINK_TYPE);
        new SFTPSymlinkFeature(session).symlink(symlink, target.getName());
        assertTrue(new SFTPFindFeature(session).find(symlink));
        final TransferStatus status = new TransferStatus();
        final byte[] content = "test".getBytes("UTF-8");
        status.setLength(content.length);
        status.setExists(true);
        final OutputStream out = new SFTPWriteFeature(session).write(symlink, status);
        IOUtils.write(content, out);
        IOUtils.closeQuietly(out);
        {
            final byte[] buffer = new byte[content.length];
            final InputStream in = new SFTPReadFeature(session).read(symlink, new TransferStatus());
            IOUtils.readFully(in, buffer);
            IOUtils.closeQuietly(in);
            assertArrayEquals(content, buffer);
        }
        {
            final byte[] buffer = new byte[0];
            final InputStream in = new SFTPReadFeature(session).read(target, new TransferStatus());
            IOUtils.readFully(in, buffer);
            IOUtils.closeQuietly(in);
            assertArrayEquals(new byte[0], buffer);
        }
        final AttributedList<Path> list = new SFTPListService(session).list(session.workdir(), new DisabledListProgressListener());
        assertTrue(list.contains(new Path(session.workdir(), name, Path.FILE_TYPE)));
        assertFalse(list.contains(symlink));
        new SFTPDeleteFeature(session).delete(Arrays.asList(target, symlink), new DisabledLoginController());
    }

    @Test(expected = NotfoundException.class)
    public void testWriteNotFound() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", new Credentials(
                properties.getProperty("sftp.user"), properties.getProperty("sftp.password")
        ));
        final SFTPSession session = new SFTPSession(host);
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final Path test = new Path(new DefaultHomeFinderService(session).find().getAbsolute() + "/nosuchdirectory/" + UUID.randomUUID().toString(), Path.FILE_TYPE);
        new SFTPWriteFeature(session).write(test, new TransferStatus());
    }

    @Test
    public void testAppend() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", new Credentials(
                properties.getProperty("sftp.user"), properties.getProperty("sftp.password")
        ));
        final SFTPSession session = new SFTPSession(host);
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final Path test = new Path(session.workdir(), UUID.randomUUID().toString(), Path.FILE_TYPE);
        assertEquals(false, new SFTPWriteFeature(session).append(
                new Path(session.workdir(), UUID.randomUUID().toString(), Path.FILE_TYPE), new TransferStatus(), Cache.empty()).append);
        assertEquals(true, new SFTPWriteFeature(session).append(
                new Path(session.workdir(), "test", Path.FILE_TYPE), new TransferStatus(), Cache.empty()).append);
    }
}
