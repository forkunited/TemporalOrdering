# TemporalOrdering #

This repository contains code for manipulating data and
training/testing models for temporal ordering tasks (the
tasks associated with TimeML http://timeml.org/site/index.html).

The bulk of the work for training and evaluating the models 
is done using the ARKWater library 
(https://github.com/forkunited/ARKWater), and the code 
in the TemporalOrdering project is mainly for deserializing and 
manipulating the data-sets and loading them into the 
ARKWater objects. 

All of the code relies on having tokenized and JSON serialized versions of the
data sets.  You can generate the JSON serialized versions of TempEval
and TimeBank datasets by downloading them and running 
*temp.scratch.ConstructTempDocumentsTempEval2* or 
*temp.scratch.ConstructTempDocumentsTempEval3* or
*temp.scratch.ConstructTempDocumentsTimeSieve* 
on them.

## Layout of the project ##

The code is organized into the following packages in the *src* directory:

* *temp.data* - Classes for cleaning temporal ordering data and performing
other miscellaneous data related tasks

* *temp.data.annotation* - Classes for loading temporal ordering documents and
annotations into memory

* *temp.data.annotation.cost* - Classes that are only helpful for experimenting
with cost function learning (https://github.com/forkunited/CostFunctionLearning)

* *temp.data.annotation.structure* - Classes that help to impose structured
constraints on the output of temporal ordering models.

* *temp.data.annotation.timeml* - Representations for time expressions, events,
and other temporal ordering objects especially from TimeML 
(http://timeml.org/site/index.html).

* *temp.data.feature* - Code for computing temporal ordering features to be
used in models.

* *temp.model.annotator.nlp* - Classes for generating NLP annotations for
text.

* *temp.model.annotator.timeml* - Classes for generating TimeML annotations
(marking events and time expressions) for text.

* *temp.scratch* - Code for performing miscellaneous tasks.  The files in this
directory contain the main functions where the code starts running.

* *temp.util* - Miscellaneous utility classes.

The *temp.scratch* package contains the entry points for the code. 
So if you're trying
to understand how this library uses ARKWater 
to train and test the temporal ordering
models, you should start by looking at classes in *temp.scratch*.

The *experiments* directory contains experiment configuration files for 
running experiments through the *Experiment* classes in *textclass.scratch* 
using ARKWater.

The *files* directory contains templates for configuration files.

The *papers* directory contains general documentation and papers related to the 
project.

## How to run things ##

Before running anything, you need to configure the project for your local 
setup.  To configure, do the following:

1. Untar the jars at *files/jars.tgz* into an appropriate location. (This
doesn't actually exist right now for this project. Just ask Jesse for the
Jars instead.)

2.  Copy *files/build.xml* and *files/temp.properties* to the top-level directory
of the project. 

3.  Fill out the copied *temp.properties* and *build.xml* files with the 
appropriate settings by replacing the text inside them that is surrounded by 
square brackets.

## Notes ##

* Some of the jars included in build.xml are probably no longer necessary.  You might
want to take some time to figure out which ones are unnecessary and remove them.

* The FreeLing library interface in *temp.model.annotator.nlp.NLPAnnotatorFreeLing* 
only works on Windows currently.