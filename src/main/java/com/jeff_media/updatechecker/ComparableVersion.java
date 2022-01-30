/*
 * Copyright (c) 2022 Alexander Majka (mfnalex), JEFF Media GbR
 * Website: https://www.jeff-media.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.jeff_media.updatechecker;

import java.math.BigInteger;
import java.util.*;

class ComparableVersion implements Comparable<ComparableVersion> {
    private String value;
    private ListItem items;

    public ComparableVersion(final String version) {
        this.parseVersion(version);
    }

    public final void parseVersion(String version) {
        this.value = version;
        this.items = new ListItem();
        version = version.toLowerCase(Locale.ENGLISH);
        ListItem list = this.items;
        final Deque<Item> stack = new ArrayDeque<>();
        stack.push(list);
        boolean isDigit = false;
        int startIndex = 0;
        for (int i = 0; i < version.length(); ++i) {
            final char c = version.charAt(i);
            if (c == '.') {
                if (i == startIndex) {
                    (list).add(IntItem.ZERO);
                } else {
                    list.add(parseItem(isDigit, version.substring(startIndex, i)));
                }
                startIndex = i + 1;
            } else if (c == '-') {
                if (i == startIndex) {
                    (list).add(IntItem.ZERO);
                } else {
                    list.add(parseItem(isDigit, version.substring(startIndex, i)));
                }
                startIndex = i + 1;
                (list).add(list = new ListItem());
                stack.push(list);
            } else if (Character.isDigit(c)) {
                if (!isDigit && i > startIndex) {
                    (list).add(new StringItem(version.substring(startIndex, i), true));
                    startIndex = i;
                    (list).add(list = new ListItem());
                    stack.push(list);
                }
                isDigit = true;
            } else {
                if (isDigit && i > startIndex) {
                    list.add(parseItem(true, version.substring(startIndex, i)));
                    startIndex = i;
                    (list).add(list = new ListItem());
                    stack.push(list);
                }
                isDigit = false;
            }
        }
        if (version.length() > startIndex) {
            list.add(parseItem(isDigit, version.substring(startIndex)));
        }
        while (!stack.isEmpty()) {
            list = (ListItem) stack.pop();
            list.normalize();
        }
    }

    private static Item parseItem(final boolean isDigit, String buf) {
        if (!isDigit) {
            return new StringItem(buf, false);
        }
        buf = stripLeadingZeroes(buf);
        if (buf.length() <= 9) {
            return new IntItem(buf);
        }
        if (buf.length() <= 18) {
            return new LongItem(buf);
        }
        return new BigIntegerItem(buf);
    }

    private static String stripLeadingZeroes(final String buf) {
        if (buf == null || buf.isEmpty()) {
            return "0";
        }
        for (int i = 0; i < buf.length(); ++i) {
            final char c = buf.charAt(i);
            if (c != '0') {
                return buf.substring(i);
            }
        }
        return buf;
    }

    @Override
    public int compareTo(final ComparableVersion o) {
        return this.items.compareTo(o.items);
    }

    @Override
    public int hashCode() {
        return this.items.hashCode();
    }

    @Override
    public boolean equals(final Object o) {
        return o instanceof ComparableVersion && this.items.equals(((ComparableVersion) o).items);
    }

    @Override
    public String toString() {
        return this.value;
    }

    private interface Item {

        int compareTo(final Item p0);

        int getType();

        boolean isNull();
    }

    private static class IntItem implements Item {
        public static final IntItem ZERO;

        static {
            ZERO = new IntItem();
        }

        private final int value;

        private IntItem() {
            this.value = 0;
        }

        IntItem(final String str) {
            this.value = Integer.parseInt(str);
        }

        @Override
        public int compareTo(final Item item) {
            if (item == null) {
                return (this.value != 0) ? 1 : 0;
            }
            switch (item.getType()) {
                case 3: {
                    final int itemValue = ((IntItem) item).value;
                    return Integer.compare(this.value, itemValue);
                }
                case 0:
                case 4: {
                    return -1;
                }
                case 1:
                case 2: {
                    return 1;
                }
                default: {
                    throw new IllegalStateException("invalid item: " + item.getClass());
                }
            }
        }

        @Override
        public int getType() {
            return 3;
        }

        @Override
        public boolean isNull() {
            return this.value == 0;
        }

        @Override
        public int hashCode() {
            return this.value;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            final IntItem intItem = (IntItem) o;
            return this.value == intItem.value;
        }

        @Override
        public String toString() {
            return Integer.toString(this.value);
        }
    }

    private static class LongItem implements Item {
        private final long value;

        LongItem(final String str) {
            this.value = Long.parseLong(str);
        }

        @Override
        public int compareTo(final Item item) {
            if (item == null) {
                return (this.value != 0L) ? 1 : 0;
            }
            switch (item.getType()) {
                case 3:
                case 1:
                case 2: {
                    return 1;
                }
                case 4: {
                    final long itemValue = ((LongItem) item).value;
                    return Long.compare(this.value, itemValue);
                }
                case 0: {
                    return -1;
                }
                default: {
                    throw new IllegalStateException("invalid item: " + item.getClass());
                }
            }
        }

        @Override
        public int getType() {
            return 4;
        }

        @Override
        public boolean isNull() {
            return this.value == 0L;
        }

        @Override
        public int hashCode() {
            return (int) (this.value ^ this.value >>> 32);
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            final LongItem longItem = (LongItem) o;
            return this.value == longItem.value;
        }

        @Override
        public String toString() {
            return Long.toString(this.value);
        }
    }

    private static class BigIntegerItem implements Item {
        private final BigInteger value;

        BigIntegerItem(final String str) {
            this.value = new BigInteger(str);
        }

        @Override
        public int compareTo(final Item item) {
            if (item == null) {
                return BigInteger.ZERO.equals(this.value) ? 0 : 1;
            }
            switch (item.getType()) {
                case 3:
                case 4:
                case 1:
                case 2: {
                    return 1;
                }
                case 0: {
                    return this.value.compareTo(((BigIntegerItem) item).value);
                }
                default: {
                    throw new IllegalStateException("invalid item: " + item.getClass());
                }
            }
        }

        @Override
        public int getType() {
            return 0;
        }

        @Override
        public boolean isNull() {
            return BigInteger.ZERO.equals(this.value);
        }

        @Override
        public int hashCode() {
            return this.value.hashCode();
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            final BigIntegerItem that = (BigIntegerItem) o;
            return this.value.equals(that.value);
        }

        @Override
        public String toString() {
            return this.value.toString();
        }
    }

    private static class StringItem implements Item {
        private static final List<String> QUALIFIERS;
        private static final Properties ALIASES;
        private static final String RELEASE_VERSION_INDEX;

        static {
            QUALIFIERS = Arrays.asList("alpha", "beta", "milestone", "rc", "snapshot", "", "sp");
            ((ALIASES = new Properties())).put("ga", "");
            (StringItem.ALIASES).put("final", "");
            (StringItem.ALIASES).put("release", "");
            (StringItem.ALIASES).put("cr", "rc");
            RELEASE_VERSION_INDEX = String.valueOf(StringItem.QUALIFIERS.indexOf(""));
        }

        private final String value;

        StringItem(String value, final boolean followedByDigit) {
            if (followedByDigit && value.length() == 1) {
                switch (value.charAt(0)) {
                    case 'a': {
                        value = "alpha";
                        break;
                    }
                    case 'b': {
                        value = "beta";
                        break;
                    }
                    case 'm': {
                        value = "milestone";
                        break;
                    }
                }
            }
            this.value = StringItem.ALIASES.getProperty(value, value);
        }

        @Override
        public int compareTo(final Item item) {
            if (item == null) {
                return comparableQualifier(this.value).compareTo(StringItem.RELEASE_VERSION_INDEX);
            }
            switch (item.getType()) {
                case 0:
                case 3:
                case 4:
                case 2: {
                    return -1;
                }
                case 1: {
                    return comparableQualifier(this.value).compareTo(comparableQualifier(((StringItem) item).value));
                }
                default: {
                    throw new IllegalStateException("invalid item: " + item.getClass());
                }
            }
        }

        @Override
        public int getType() {
            return 1;
        }

        @Override
        public boolean isNull() {
            return comparableQualifier(this.value).compareTo(StringItem.RELEASE_VERSION_INDEX) == 0;
        }

        public static String comparableQualifier(final String qualifier) {
            final int i = StringItem.QUALIFIERS.indexOf(qualifier);
            return (i == -1) ? (StringItem.QUALIFIERS.size() + "-" + qualifier) : String.valueOf(i);
        }

        @Override
        public int hashCode() {
            return this.value.hashCode();
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            final StringItem that = (StringItem) o;
            return this.value.equals(that.value);
        }

        @Override
        public String toString() {
            return this.value;
        }
    }

    private static class ListItem extends ArrayList<Item> implements Item {
        void normalize() {
            for (int i = this.size() - 1; i >= 0; --i) {
                final Item lastItem = this.get(i);
                if (lastItem.isNull()) {
                    this.remove(i);
                } else if (!(lastItem instanceof ListItem)) {
                    break;
                }
            }
        }

        @Override
        public int compareTo(final Item item) {
            if (item == null) {
                if (this.size() == 0) {
                    return 0;
                }
                final Item first = this.get(0);
                return first.compareTo(null);
            } else {
                switch (item.getType()) {
                    case 0:
                    case 3:
                    case 4: {
                        return -1;
                    }
                    case 1: {
                        return 1;
                    }
                    case 2: {
                        final Iterator<Item> left = this.iterator();
                        final Iterator<Item> right = ((ListItem) item).iterator();
                        while (left.hasNext() || right.hasNext()) {
                            final Item l = left.hasNext() ? left.next() : null;
                            final Item r = right.hasNext() ? right.next() : null;
                            //noinspection ConstantConditions
                            final int result = (l == null) ? ((r == null) ? 0 : (-1 * r.compareTo(l))) : l.compareTo(r);
                            if (result != 0) {
                                return result;
                            }
                        }
                        return 0;
                    }
                    default: {
                        throw new IllegalStateException("invalid item: " + item.getClass());
                    }
                }
            }
        }

        @Override
        public int getType() {
            return 2;
        }

        @Override
        public boolean isNull() {
            return this.size() == 0;
        }

        @Override
        public String toString() {
            final StringBuilder buffer = new StringBuilder();
            for (final Item item : this) {
                if (buffer.length() > 0) {
                    buffer.append((item instanceof ListItem) ? '-' : '.');
                }
                buffer.append(item);
            }
            return buffer.toString();
        }
    }
}
