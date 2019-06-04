package ch.cyberduck.core.b2;

/*
 * Copyright (c) 2002-2019 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.transfer.TransferStatus;

import java.util.Collections;

public class B2MoveFeature implements Move {

    private final PathContainerService containerService
        = new B2PathContainerService();

    private final B2Session session;
    private final B2FileidProvider fileid;

    public B2MoveFeature(final B2Session session, final B2FileidProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public Path move(final Path source, final Path target, final TransferStatus status, final Delete.Callback delete, final ConnectionCallback callback) throws BackgroundException {
        final Path copy = new B2CopyFeature(session, fileid).copy(source, target, status.length(source.attributes().getSize()), callback);
        new B2DeleteFeature(session, fileid).delete(Collections.singletonList(new Path(source)), callback, delete);
        return copy;
    }

    @Override
    public boolean isSupported(final Path source, final Path target) {
        return !containerService.isContainer(source);
    }

    @Override
    public boolean isRecursive(final Path source, final Path target) {
        return new B2CopyFeature(session, fileid).isRecursive(source, target);
    }

    @Override
    public Move withDelete(final Delete delete) {
        return this;
    }
}
