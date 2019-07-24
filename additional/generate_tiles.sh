#!/usr/bin/env bash
@echo off

echo 'should already have original image in folder, as well as folders named tiles and samples'

export basename=zoo_map_base_name
export filename=zoo_map.jpg
export extension=jpg

export imagemagick=/usr/bin/convert
export tilesize=256
export samplesize=500

export tilesfolder='./tiles'
export samplesfolder='./samples'

sudo echo create tile folders
sudo mkdir $tilesfolder/$basename
sudo mkdir $tilesfolder/$basename/1000
sudo mkdir $tilesfolder/$basename/500
sudo mkdir $tilesfolder/$basename/250
sudo mkdir $tilesfolder/$basename/125

echo "create half-sized versions for tiling (will be discarded later)"
sudo $imagemagick $filename -resize 50%%  $basename-500.$extension
sudo $imagemagick $filename -resize 25%%  $basename-250.$extension
sudo $imagemagick $filename -resize 12.5%%  $basename-125.$extension

echo create sample
sudo $imagemagick $filename -thumbnail $samplesizex$samplesize  ./$samplesfolder/$filename

echo create tiles
#convert zoo_map.jpg -crop 200x200 -set filename:tile "%[fx:page.x/200],%[fx:page.y/200]" +repage +adjoin "tiles/%[filename:tile].png"

sudo $imagemagick $filename -crop $tilesize'x'$tilesize -set filename:tile "%[fx:page.x/$tilesize]_%[fx:page.y/$tilesize]" +repage +adjoin "$tilesfolder/$basename/1000/%[filename:tile].$extension"
sudo $imagemagick $basename-500.$extension -crop $tilesize'x'$tilesize -set filename:tile "%[fx:page.x/$tilesize]_%[fx:page.y/$tilesize]" +repage +adjoin "$tilesfolder/$basename/500/%[filename:tile].$extension"
sudo $imagemagick $basename-250.$extension -crop $tilesize'x'$tilesize -set filename:tile "%[fx:page.x/$tilesize]_%[fx:page.y/$tilesize]" +repage +adjoin "$tilesfolder/$basename/250/%[filename:tile].$extension"
sudo $imagemagick $basename-125.$extension -crop $tilesize'x'$tilesize -set filename:tile "%[fx:page.x/$tilesize]_%[fx:page.y/$tilesize]" +repage +adjoin "$tilesfolder/$basename/125/%[filename:tile].$extension"


echo cleanup
sudo rm $basename-500.$extension
sudo rm $basename-250.$extension
sudo rm $basename-125.$extension

echo DONE

