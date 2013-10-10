#!/usr/bin/perl

#session|querynum|query|numfound|pages|duration|clicks|terms|phrases|boolop|expr|wild|topic|author|language|country|pubdate|pubtrie|format|htsource|lmt|incommon|tophrase|addterms|rmterms|editdist|addop|addfacet|rmfacet|addft|rmft
#09371e135d85fbb70b246761ce9294b6 ab72ebb68315db6bf14300786b8f0cc2|2|The Smart Set|21|1|67000|0|3|0|0|0|0|0|0|1|0|0|0|0|0|1|0|1|0|0|0|0|21|1|0|0|0|0|140354
#09371e135d85fbb70b246761ce9294b6 ab72ebb68315db6bf14300786b8f0cc2|3|The Smart Set|4526|1|26000|0|3|0|0|0|0|0|0|1|0|0|0|0|0|1|0|1|0|0|0|0|11|0|0|0|0|0|140357
#09371e135d85fbb70b246761ce9294b6 ab72ebb68315db6bf14300786b8f0cc2|4|"The Smart Set"|128|1|53000|0|3|1|0|0|0|0|0|0|0|0|0|0|0|1|0|1|0|0|0|0|5|0|0|1|0|0|140359
#09371e135d85fbb70b246761ce9294b6 ab72ebb68315db6bf14300786b8f0cc2|5|"The Smart Set"|5|1|0|0|3|1|0|0|0|0|0|0|0|0|1|0|0|1|0|1|0|0|0|0|12|0|1|0|0|0|140361


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

