@echo off
echo %time:~0,8% Start

rem == Change version and build number in Constants.java
rem == Maybe increase TCP_DRIVER_VERSION (old clients must be compatible!)
rem == Update the changelog (add new version)
rem == Update the newsfeed
rem == No "  Message.get" (must be "throw Message.get")
rem == Documentation: check if all Javadoc files are in the index
rem == Check that is no TODO in the docs
rem == Check code coverage
rem == Run regression test with JDK 1.4 and 1.5
rem == Use latest versions of other dbs
rem == Change version(s) in performance.html

setlocal
cd ../..
set today=%date:~6%%date:~3,2%%date:~0,2%
rmdir /s /q ..\h2web-%today% 2>nul
rmdir /s /q ..\h2web 2>nul
mkdir ..\h2web

rmdir /s /q bin 2>nul
rmdir /s /q temp 2>nul
call java14 >nul 2>nul
call build -quiet

call java16 >nul 2>nul
call build -quiet compile
call build -quiet spellcheck javadocImpl jarClient

echo %time:~0,8% JDK 1.4
call java14 >nul 2>nul
call build -quiet clean compile
call build -quiet installer mavenDeployCentral

rem call build -quiet compile benchmark
rem == Copy the benchmark results and update the performance page and diagram

call java16 >nul 2>nul
call build -quiet switchSource
ren ..\h2web h2web-%today%

echo %time:~0,8% Done

rem == Test with Hibernate
rem == Run FindBugs
rem == buildRelease.bat
rem == Check docs, versions and links in main, downloads, build numbers
rem == Check if missing javadocs
rem == Test installer
rem == Check in the PDF file:
rem == - footer
rem == - front page
rem == - orphan control
rem == - check images
rem == - table of contents
rem == Test Console
rem == Test all languages
rem == Scan for viruses
rem == Upload to SourceForge
rem == svn commit
rem == svn copy: /svn/trunk /svn/tags/version-1.1.x; Version 1.1.x (yyyy-mm-dd)
rem == Newsletter: prepare (always to BCC!!)
rem == Upload to h2database.com, http://code.google.com/p/h2database/downloads/list
rem == Newsletter: send (always to BCC!!)
rem == Remove Contacts in GMail
rem == Add to freshmeat
rem == Change version: http://en.wikipedia.org/wiki/H2_%28DBMS%29
rem == Change version: http://ja.wikipedia.org/wiki/H2_Database
rem == Change version: http://es.wikipedia.org/wiki/H2
rem == Change version: http://www.heise.de/software/
rem == Close bugs: http://code.google.com/p/h2database/issues/list
