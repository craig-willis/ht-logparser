#!/usr/bin/perl

#
# Rough script to output the number of records found, pages viewed, duration, 
# and clicks when clicks > 1.
#
# Input: queries.out
#

print "query, numfound, pages, duration, clicks, lmt\n";
while (<>)
{
   $_ =~ s/\n//g;
   my ($session, $queryNum, $query, $numfound, $pages, $duration, $clicks, $terms, $phrases, $boolop, $expr, $wild, $topic, $author, $language, $country, $pubdate, $pubtrie, $format, $htsource, $lmt) = split /\|/, $_;
   my ($ipaddr, $session) = split / /, $session;
   $query =~ s/"/XXXX/g;
   $query =~ s/\p{Punct}/ /g;
   $query =~ s/XXXX/"/g;
   $query =~ s/\s+/ /g;
   if ($topic == 0 && $author == 0 && $language == 0 && $country == 0 && $pubdate == 0 && $pubtrie == 0 && $format == 0 && $htsource ==0 && $clicks > 1 ) {
      print "$query, $numfound, $pages, $duration, $clicks, $lmt\n";
   }
}

