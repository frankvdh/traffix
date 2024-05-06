<?php
$xml = simplexml_load_file($argv[1]);
$attr = $xml->attributes();
$width = $attr->width;
if($width == "100%") {
    $viewBox = preg_split('/[\s,]+/', $attr->viewBox ?: '');
    $width = isset($viewBox[2]) ? (float) $viewBox[2] - (float)$viewBox[0] : 0;
}
printf("%s", $width);
