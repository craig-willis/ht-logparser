#!/usr/bin/perl

use Digest::MD5 qw( md5);
use Time::Local;
use utf8;
use POSIX;


my %queries;
while (<>) 
{
    $_ =~ s/\n//g;
    my @fields = split (/\|/, $_);
    my $queryid = $fields[0]; 
    my $querynum = $fields[1]; 
    my $query = $fields[2]; 
    my $numfound = $fields[3]; 
    my $pages = $fields[4]; 
    my $duration = $fields[5]; 
    my $clicks = $fields[6]; 
    my $terms = $fields[7]; 
    my $phrases = $fields[8]; 
    my $boolop = $fields[9]; 
    my $expr = $fields[10]; 
    my $wild = $fields[11]; 
    my $topic = $fields[12]; 
    my $author = $fields[13]; 
    my $language = $fields[14]; 
    my $country = $fields[15]; 
    my $pubdate = $fields[16]; 
    my $pubtrie = $fields[17]; 
    my $format = $fields[18]; 
    my $htsource = $fields[19]; 
    my $rights = $fields[20]; 
    my $recall = 0;

    my $queryid = "$queryid $querynum";

    $queries{$queryid}{"numfound"} = $numfound;
    $queries{$queryid}{"pages"} = $pages;
    $queries{$queryid}{"duration"} += $duration;
    $queries{$queryid}{"clicks"} += $clicks;
    $queries{$queryid}{"terms"} = $terms;
    $queries{$queryid}{"phrases"} += $phrases;
    $queries{$queryid}{"boolop"} += $boolop;
    $queries{$queryid}{"expr"} += $expr;
    $queries{$queryid}{"wild"} += $wild;
    $queries{$queryid}{"topic"} += $topic;
    $queries{$queryid}{"author"} += $author;
    $queries{$queryid}{"language"} += $language;
    $queries{$queryid}{"country"} += $country;
    $queries{$queryid}{"pubdate"} += $pubdate;
    $queries{$queryid}{"pubtrie"} += $pubtrie;
    $queries{$queryid}{"format"} =+ $format;
    $queries{$queryid}{"htsource"} += $htsource;
    $queries{$queryid}{"rights"} += $rights;
    $queries{$queryid}{"facets"} += $topic + $author + $language + $country + $pubdate + $pubtrie + $format + $htsource;
}

print "queryid,numfound,pages,duration,clicks,terms,phrases,boolop,expr,wild,topic,author,language,country,pubdate,pubtrie,format,htsource,rights,facets,recall\n";
for $queryid ( keys %queries) {
    my $numfound = $queries{$queryid}{"numfound"};
    my $pages = $queries{$queryid}{"pages"};
    my $recall = 0;
    if ($numfound > 0) {
       $recall =  sprintf "%.2f", ($pages / (floor($numfound/26)+1));
    }
    print $queryid . "," . 
         $queries{$queryid}{"numfound"} . "," .
         $queries{$queryid}{"pages"} . "," .
         $queries{$queryid}{"duration"} . "," .
         $queries{$queryid}{"clicks"} . "," .
         $queries{$queryid}{"terms"} . "," .
         $queries{$queryid}{"phrases"} . "," .
         $queries{$queryid}{"boolop"} . "," .
         $queries{$queryid}{"expr"} . "," .
         $queries{$queryid}{"wild"} . "," .
         $queries{$queryid}{"topic"} . "," .
         $queries{$queryid}{"author"} . "," .
         $queries{$queryid}{"language"} . "," .
         $queries{$queryid}{"country"} . "," .
         $queries{$queryid}{"pubdate"} . "," .
         $queries{$queryid}{"pubtrie"} . "," .
         $queries{$queryid}{"format"} . "," .
         $queries{$queryid}{"htsource"} . "," .
         $queries{$queryid}{"rights"} . "," .
         $queries{$queryid}{"facets"} . "," . 
         $recall . "\n";
}
