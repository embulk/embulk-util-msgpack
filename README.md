embulk-util-msgpack
====================

*NOTE: No future compatibility is guaranteed as of v0.8.24. All classes in this library may go away.*

This is a utility with the MessagePack format.

It started from a fork of [MessagePack for Java (`msgpack-java`)](https://github.com/msgpack/msgpack-java) so that it would not depend on `msgpack-java`. Plugin's `msgpack-java` could conflict because the Embulk SPI has had a dependency on `msgpack-java` by itself.

Once the Embulk SPI removes the dependency on `msgpack-java`, as planned in [EEP-2](https://github.com/embulk/embulk/blob/master/docs/eeps/eep-0002.md), it would eventually depend on `msgpack-java`, and remove the copy.

For Embulk plugin developers
-----------------------------

* [Javadoc](https://dev.embulk.org/embulk-util-msgpack/)

For Maintainers
----------------

### Release

Modify `version` in `build.gradle` at a detached commit, and then tag the commit with an annotation.

```
git checkout --detach master

(Edit: Remove "-SNAPSHOT" in "version" in build.gradle.)

git add build.gradle

git commit -m "Release vX.Y.Z"

git tag -a vX.Y.Z

(Edit: Write a tag annotation in the changelog format.)
```

See [Keep a Changelog](https://keepachangelog.com/en/1.0.0/) for the changelog format. We adopt a part of it for Git's tag annotation like below.

```
## [X.Y.Z] - YYYY-MM-DD

### Added
- Added a feature.

### Changed
- Changed something.

### Fixed
- Fixed a bug.
```

Push the annotated tag, then. It triggers a release operation on GitHub Actions after approval.

```
git push -u origin vX.Y.Z
```
