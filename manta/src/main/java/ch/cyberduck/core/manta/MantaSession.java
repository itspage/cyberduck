package ch.cyberduck.core.manta;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostKeyCallback;
import ch.cyberduck.core.HostPasswordStore;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.UrlProvider;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Home;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.IOException;
import java.security.Security;

import com.joyent.manta.client.MantaClient;
import com.joyent.manta.client.MantaObject;
import com.joyent.manta.config.BaseChainedConfigContext;
import com.joyent.manta.config.ChainedConfigContext;
import com.joyent.manta.config.DefaultsConfigContext;
import com.joyent.manta.config.SettableConfigContext;
import com.joyent.manta.config.StandardConfigContext;
import com.joyent.manta.exception.MantaClientHttpResponseException;
import com.joyent.manta.exception.MantaException;

public class MantaSession extends Session<MantaClient> {
    private static final Logger log = Logger.getLogger(MantaSession.class);

    static {
        final int position = PreferencesFactory.get().getInteger("connection.ssl.provider.bouncycastle.position");
        final BouncyCastleProvider provider = new BouncyCastleProvider();
        if(log.isInfoEnabled()) {
            log.info(String.format("Install provider %s at position %d", provider, position));
        }
        Security.insertProviderAt(provider, position);
    }

    public MantaSession(final Host host) {
        super(host);
    }

    @Override
    protected MantaClient connect(final HostKeyCallback key) throws BackgroundException {
        final SettableConfigContext<BaseChainedConfigContext> config = new ChainedConfigContext(
                new DefaultsConfigContext(),
                new StandardConfigContext()
                        .setHttpsProtocols(PreferencesFactory.get().getProperty("connection.ssl.protocols"))
                        .setDisableNativeSignatures(true)
                        .setNoAuth(false)
                        .setMantaUser(host.getCredentials().getUsername())
                        .setMantaURL(String.format("%s://%s", host.getProtocol().getScheme().name(), host.getHostname()))
        );
        return new MantaClient(config);
    }

    @Override
    public void login(final HostPasswordStore keychain, final LoginCallback prompt, final CancelCallback cancel, final Cache<Path> cache) throws BackgroundException {
        try {
            final SettableConfigContext config = (SettableConfigContext) client.getContext();
            final String user = host.getCredentials().getUsername();
            config.setMantaUser(user);
            config.setMantaKeyId(new MantaPublicKeyAuthentication(this).authenticate(host, keychain, prompt, cancel));
            // Instantiation of client does not validate credentials. List the home path to test the connection
            final MantaAccountHomeInfo account = new MantaAccountHomeInfo(host.getCredentials().getUsername(), host.getDefaultPath());
            client.isDirectoryEmpty(account.getNormalizedHomePath().getAbsolute());
        }
        catch(MantaException e) {
            throw new MantaExceptionMappingService().map(e);
        }
        catch(MantaClientHttpResponseException e) {
            throw new MantaHttpExceptionMappingService().map(e);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }

    @Override
    protected void logout() throws BackgroundException {
        if(client != null) {
            client.closeWithWarning();
        }
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        return new MantaListService(this).list(directory, listener);
    }

    protected boolean userIsOwner() throws IllegalStateException {
        final MantaAccountHomeInfo account = new MantaAccountHomeInfo(host.getCredentials().getUsername(), host.getDefaultPath());
        return StringUtils.equals(host.getCredentials().getUsername(),
                account.getAccountOwner());
    }

    protected Path getNormalizedHomePath() {
        return new MantaAccountHomeInfo(host.getCredentials().getUsername(), host.getDefaultPath()).getNormalizedHomePath();
    }

    protected Path getAccountRoot() {
        return new MantaAccountHomeInfo(host.getCredentials().getUsername(), host.getDefaultPath()).getAccountRoot();
    }

    protected String getAccountOwner() {
        return new MantaAccountHomeInfo(host.getCredentials().getUsername(), host.getDefaultPath()).getAccountOwner();
    }

    protected Path getAccountPublicRoot() {
        return new MantaAccountHomeInfo(host.getCredentials().getUsername(), host.getDefaultPath()).getAccountPublicRoot();
    }

    protected Path getAccountPrivateRoot() {
        return new MantaAccountHomeInfo(host.getCredentials().getUsername(), host.getDefaultPath()).getAccountPrivateRoot();
    }

    protected boolean isUserWritable(final MantaObject mantaObject) {
        final MantaAccountHomeInfo account = new MantaAccountHomeInfo(host.getCredentials().getUsername(), host.getDefaultPath());
        return StringUtils.startsWithAny(
                mantaObject.getPath(),
                account.getAccountPublicRoot().getAbsolute(),
                account.getAccountPrivateRoot().getAbsolute());
    }

    protected boolean isUserWritable(final Path path) {
        final MantaAccountHomeInfo account = new MantaAccountHomeInfo(host.getCredentials().getUsername(), host.getDefaultPath());
        return path.equals(account.getAccountPublicRoot())
                || path.equals(account.getAccountPrivateRoot())
                || path.isChild(account.getAccountPublicRoot())
                || path.isChild(account.getAccountPrivateRoot());
    }

    protected boolean isWorldReadable(final MantaObject mantaObject) {
        final MantaAccountHomeInfo accountHomeInfo = new MantaAccountHomeInfo(host.getCredentials().getUsername(), host.getDefaultPath());
        return StringUtils.startsWithAny(
                mantaObject.getPath(),
                accountHomeInfo.getAccountPublicRoot().getAbsolute());
    }

    protected boolean isWorldReadable(final Path path) {
        final MantaAccountHomeInfo account = new MantaAccountHomeInfo(host.getCredentials().getUsername(), host.getDefaultPath());
        return path.isChild(account.getAccountPublicRoot());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T _getFeature(final Class<T> type) {
        if(type == Directory.class) {
            return (T) new MantaDirectoryFeature(this);
        }
        else if(type == Read.class) {
            return (T) new MantaReadFeature(this);
        }
        else if(type == Write.class) {
            return (T) new MantaWriteFeature(this);
        }
        else if(type == Delete.class) {
            return (T) new MantaDeleteFeature(this);
        }
        else if(type == Touch.class) {
            return (T) new MantaTouchFeature(this);
        }
        else if(type == Move.class) {
            return (T) new MantaMoveFeature(this);
        }
        else if(type == AttributesFinder.class) {
            return (T) new MantaAttributesFinderFeature(this);
        }
        else if(type == UrlProvider.class) {
            return (T) new MantaUrlProviderFeature(this);
        }
        else if(type == Home.class) {
            return (T) new MantaHomeFinderFeature(this);
        }
        return super._getFeature(type);
    }
}
