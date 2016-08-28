/*
 * Copyright (C) 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.copybara;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.devtools.build.lib.skylarkinterface.SkylarkCallable;
import com.google.devtools.build.lib.skylarkinterface.SkylarkModule;
import com.google.devtools.build.lib.skylarkinterface.SkylarkModuleCategory;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents the contributor of a change in the destination repository. A contributor can be
 * either an individual or a team.
 *
 * <p>Author is lenient in name or email validation.
 */
@SkylarkModule(name = "author",
    category = SkylarkModuleCategory.BUILTIN,
    doc = "Represents the author of a change")
public final class Author {

  private static final Pattern AUTHOR_PARSER = Pattern.compile("(?<name>[^<]+)<(?<email>[^>]+)>");

  private final String name;
  private final String email;

  public Author(String name, String email) {
    this.name = Preconditions.checkNotNull(name);
    this.email = Preconditions.checkNotNull(email);
  }

  /**
   * Returns the name of the author.
   */
  @SkylarkCallable(name = "name", doc = "The name of the author")
  public String getName() {
    return name;
  }

  /**
   * Returns the email address of the author.
   */
  @SkylarkCallable(name = "email", doc = "The email of the author")
  public String getEmail() {
    return email;
  }

  /**
   * Returns the string representation of an author, which is the standard format
   * {@code Name <email>} used by most version control systems.
   */
  @Override
  public String toString() {
    return String.format("%s <%s>", name, email);
  }

  @Override
  public boolean equals(Object o) {
    // TODO(copybara-team): We should revisit this. Email should be the identifier to be able to
    // match two users with slightly different names "Foo B <foo@bar.com>" and
    // "Foo Bar <foo@bar.com>", but email can be empty in Git standard author format.
    if (o instanceof Author) {
      Author that = (Author) o;
      return Objects.equal(this.name, that.name)
          && Objects.equal(this.email, that.email);
    }
    return false;
  }

  /** Parse author from a String in the format of: "name <foo@bar.com>" */
  public static Author parse(String authorStr) throws ConfigValidationException {
    Matcher matcher = AUTHOR_PARSER.matcher(authorStr);
    if (!matcher.matches()) {
      throw new ConfigValidationException("Author '" + authorStr
          + "' doesn't match the expected format 'name <mail@example.com>");
    }
    return new Author(matcher.group("name").trim(), matcher.group("email").trim());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(this.name, this.email);
  }
}
