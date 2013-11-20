package ch.cyberduck.core.dav;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
 * http://cyberduck.ch/
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
 *
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.ch
 */

import ch.cyberduck.core.Cache;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Find;

import java.io.IOException;

import com.github.sardine.impl.SardineException;

/**
 * @version $Id$
 */
public class DAVFindFeature implements Find {

    private DAVSession session;

    public DAVFindFeature(final DAVSession session) {
        this.session = session;
    }

    @Override
    public boolean find(final Path file) throws BackgroundException {
        if(file.isRoot()) {
            return true;
        }
        try {
            try {
                return session.getClient().exists(new DAVPathEncoder().encode(file));
            }
            catch(SardineException e) {
                throw new DAVExceptionMappingService().map("Cannot read file attributes", e, file);
            }
            catch(IOException e) {
                throw new DefaultIOExceptionMappingService().map(e, file);
            }
        }
        catch(AccessDeniedException e) {
            // Parent directory may not be accessible. Issue #5662
            return true;
        }
        catch(NotfoundException e) {
            return false;
        }
    }

    @Override
    public Find withCache(Cache cache) {
        return this;
    }
}
