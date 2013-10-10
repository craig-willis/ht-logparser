#!/usr/bin/perl

use Digest::MD5 qw( md5);
use Time::Local;
use utf8;


my %sessions;
while (<>) 
{
    $_ =~ s/\n//g;
    my @fields = split (/\|/, $_);
    my $session = $fields[0]; 
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
 
    $sessions{$session}{"queries"} ++;
    $sessions{$session}{"numfound"} += $numfound;
    $sessions{$session}{"pages"} += $pages;
    $sessions{$session}{"duration"} += $duration;
    $sessions{$session}{"clicks"} += $clicks;
    $sessions{$session}{"terms"} += $terms;
    $sessions{$session}{"phrases"} += $phrases;
    $sessions{$session}{"boolop"} += $boolop;
    $sessions{$session}{"expr"} += $expr;
    $sessions{$session}{"wild"} += $wild;
    $sessions{$session}{"topic"} += $topic;
    $sessions{$session}{"author"} += $author;
    $sessions{$session}{"language"} += $language;
    $sessions{$session}{"country"} += $countryn;
    $sessions{$session}{"pubdate"} += $pubdate;
    $sessions{$session}{"pubtrie"} += $pubtrie;
    $sessions{$session}{"format"} += $format;
    $sessions{$session}{"htsource"} += $htsource;
    $sessions{$session}{"rights"} += $rights;
}

print "session,queries,numfound,pages,duration,clicks,terms,phrases,boolop,expr,wild,topic,author,language,country,pubdate,pubtrie,format,htsource,rights\n";
my $sessnum = 0;
for $session (keys %sessions) {
    print $session . "," . $sessions{$session}{"queries"} . "," .
         $sessions{$session}{"numfound"} . "," .
         $sessions{$session}{"pages"} . "," .
         $sessions{$session}{"duration"} . "," .
         $sessions{$session}{"clicks"} . "," .
         $sessions{$session}{"terms"} . "," .
         $sessions{$session}{"phrases"} . "," .
         $sessions{$session}{"boolop"} . "," .
         $sessions{$session}{"expr"} . "," .
         $sessions{$session}{"wild"} . "," .
         $sessions{$session}{"topic"} . "," .
         $sessions{$session}{"author"} . "," .
         $sessions{$session}{"language"} . "," .
         $sessions{$session}{"country"} . "," .
         $sessions{$session}{"pubdate"} . "," .
         $sessions{$session}{"pubtrie"} . "," .
         $sessions{$session}{"format"} . "," .
         $sessions{$session}{"htsource"} . "," .
         $sessions{$session}{"rights"} . "\n";
   $sessnum++;
}
