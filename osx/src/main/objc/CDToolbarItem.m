/*
 *  Copyright (c) 2016 David Kocher. All rights reserved.
 *  https://cyberduck.io/
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

#import "CDToolbarItem.h"

@implementation CDToolbarItem

- (void)validate
{
    if ([[self toolbar] delegate])
    {
        BOOL enabled = YES;
        if ([[[self toolbar] delegate] respondsToSelector:@selector(validateToolbarItem:)]) {
           enabled = [[[self toolbar] delegate] validateToolbarItem:self];
        }
        else if ([[[self toolbar] delegate] respondsToSelector:@selector(validateUserInterfaceItem:)]) {
            enabled = [[[self toolbar] delegate] validateUserInterfaceItem:self];
        }
        [self setEnabled:enabled];
    }
    else if ([self action])
    {
        if (![self target]) {
            [self setEnabled:[[[[self view] window] firstResponder] respondsToSelector:[self action]]];
        }
        else {
            [self setEnabled:[[self target] respondsToSelector:[self action]]];
        }
    }
    else {
        [super validate];
    }
}

@end
