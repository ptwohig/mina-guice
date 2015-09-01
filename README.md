**Guice Support for Apache MINA**

This is a simple library which allows you to integrate your Apache MINA project with Guice 3.0.  This allows you to configure as much or as little of your MINA application in Guice's IoC container as you wish.

**Features**

* Support for MINA 2.0.9
* Support for Guice 3.0
* No dependencies on third-party libraries, except Apache MINA and Guice 3.0
* No need for multibindings.
* Automatic linknig of IoFilter to javax.inject.Named

**Motivaion**

This was created for a personal project some time ago.  I just started tinkering again with the personal project and decided to pull this out of my archives.  If you look at the code history it's a bit rough.  Pleae let me know if you find it useful, or if you find a bug.

**Other Notices**

I am not affiliated with the Apache MINA project in any way.  I named the packages to be consistent with MINA's naming convention.  I hope that this may be useful enough to pull into the main Apache MINA project at some point.

**License**

The MIT License (MIT)

Copyright (c) 2015 Patrick Twohig, Namazu Studios LLC

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.

