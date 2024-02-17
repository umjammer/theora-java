[![Release](https://jitpack.io/v/umjammer/theora-java.svg)](https://jitpack.io/#umjammer/theora-java)
[![Java CI](https://github.com/umjammer/theora-java/actions/workflows/maven.yml/badge.svg)](https://github.com/umjammer/theora-java/actions/workflows/maven.yml)
[![CodeQL](https://github.com/umjammer/theora-java/actions/workflows/codeql-analysis.yml/badge.svg)](https://github.com/umjammer/theora-java/actions/workflows/codeql-analysis.yml)
![Java](https://img.shields.io/badge/Java-17-b07219)

# theora-java

<img alr="theora logo" src="https://upload.wikimedia.org/wikipedia/commons/5/57/Theora_logo_2007.svg" width="129 "/><sub><a href="https://www.theora.org/">© Xiph.Org</a></sub>

Theora for java via jna.

## Install

 * [maven](https://jitpack.io/#umjammer/theora-java)

## Usage

## Reference

## TODO

---

# [Original](http://fmj-sf.net/theora-java/getting_started.php)

theora-jna is a Java wrapper around theora, using JNA.

It assumes that dynamic libraries for ogg, vorbis, and theora have been compiled, and are in your library path.

It does not require jheora, jogg, or jorbis to be in your classpath.

Step 1:
Get ogg, vorbis, theora sources from http://www.theora.org/

This library was built/tested with:
    * libtheora-1.0alpha7
    * libogg-1.1.3
    * libvorbis-1.1.2


Step 2: build/install ogg, vorbis, theora, by running the standard build procedure in each of the 3 directories:

./configure
make
sudo make install

Step 3. run the sample program with a URL to a media file (file://, http://, etc) as the first parameter, example:

java -cp ./theora-java.jar:./lib/jna.jar net.sf.theora_java.PlayerExample http://upload.wikimedia.org/wikipedia/commons/d/d0/Apollo_15_liftoff_from_inside_LM.ogg

This should show the video and/or play the audio.

You may need to set your library path in order to find the installed theora dynamic libraries, for example:

export LD_LIBRARY_PATH=/usr/local/lib

