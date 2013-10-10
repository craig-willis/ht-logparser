#!/usr/bin/perl


#
# Used to generate the queries.out file for analysis. 
# Reads clicks.out (output of clicks.pl) and solr.out (output of LogAligner)
#
#
# For each unique session, find each unique query and print the following:
#   session     combination of session + ipaddr
#   querynum    index of query in session 
#   query       query string
#   numfound    number of results found
#   pages       number of pages viewed
#   duration    duration
#   clicks      number of clicks
#   terms       number of terms in query
#   phrases     number of phrases in query
#   boolop      number of boolean operators
#   expr        number of compress expressions
#   wild        number of wildcards
#   topic       topic facet present (0/1)
#   author      author facet present (0/1)
#   language    language facet present (0/1)
#   country     country facet present (0/1)
#   pubdate     pubdate facet present (0/1)
#   pubtrie     pubtrie facet present (0/1)
#   format      format facet present (0/1)
#   htsource    source facet present (0/1)
#   lmt         ft limiter present (0/1)
#   incommon    number of terms in common with previous query in session
#   newquery    0/1 is new query in session (incommon = 0)
#   tophrase    this query is same as previous query, just converted to phrase query
#   rmphrase    quotes removed from previous query
#   addterms    terms were added to previous query
#   rmterms     terms were removed from previous query
#   editdist    edit distance (levenstein) from previous query
#   addop       operator was added to previous query
#   addfacet    facets added to previous query
#   rmfacet     facets removed from previous query  
#   addft       ft limiter enabled from previous query
#   rmft        ft limiter removed from previous query
#
# A session is defined as:
#    $session = "$ipaddr $session";
# 
# A unique query is defined as:
#    $querystr = "$query|$topic|$author|$language|$country|$pubdate|$pubtrie|$format|$htsource|$lmt|$field1";

use Text::Levenshtein qw(distance);
use Digest::MD5 qw( md5);
use Time::Local;
use utf8;


my $queryNum = 0;
my %lastSession;
my %lastQuery;
my %lastNumFound;
my %lastPage;
my %lastSeconds;

my %click_map;
open(CLICKS, "clicks.out");
while (<CLICKS>) {
   $_ =~ s/\n//g;
   my ($solr_id, $ls_id, $cnt) = split(/\|/, $_);
   $click_map{$solr_id} = $cnt; 
}
close(CLICKS);

open(SOLR, "solr.out");
while (<SOLR>) 
{
    $_ =~ s/\n//g;
    my @fields = split (/\|/, $_);
    my $id = $fields[0]; 
    my $ipaddr = $fields[1]; 
    my $session = $fields[2]; 
    my $seconds = $fields[3]; 
    my $anyall1 = $fields[4]; 
    my $field1 = $fields[5]; 
    my $q1 = $fields[6]; 
    my $op2 = $fields[7]; 
    my $anyall2 = $fields[8]; 
    my $field2 = $fields[9]; 
    my $q2 = $fields[10]; 
    my $page = $fields[11]; 
    my $lmt = $fields[12]; 
    my $topic = $fields[13]; 
    my $author = $fields[14]; 
    my $language = $fields[15]; 
    my $country = $fields[16]; 
    my $pubdate = $fields[17]; 
    my $pubtrie = $fields[18]; 
    my $format = $fields[19]; 
    my $htsource = $fields[20]; 
    my $numfound = $fields[21]; 
    my $url = $fields[22]; 
    my $referer = $fields[23]; 
   
    my $clicks = $click_map{$id};

    my $query = "$q1 $op2 $q2";
    $query =~ s/^\s+//g;
    $query =~ s/\s+$//g;

    $session = "$ipaddr $session";
 
    my $querystr = "$query|$topic|$author|$language|$country|$pubdate|$pubtrie|$format|$htsource|$lmt|$field1";
    my $querymd5 = md5($query);


    if ($lastSession{$session} == 1) {
       if ($querystr eq $lastQuery{$session}) {
          if ($numfound eq $lastNumFound{$session}) {
             if ($page eq $lastPage{$session}) {
                # This is a click return
                # Delta - how long was spent looking at the result?

                $delta = $seconds - $lastSeconds{$session};
                $pages{$session}{$querystr}{$page}{"duration"} += $delta;
                $queries{$session}{$queryNum}{"duration"} += $delta;
                $sessions{$session}{"duration"} += $delta;
             }
             else {

                # Same query, new page
                $queries{$session}{$queryNum}{"pages"} += 1;
                $sessions{$session}{"pages"} += 1;

                # How long was spent looking at the last page?
                $delta = $seconds - $lastSeconds{$session};
                $pages{$session}{$querystr}{$lastPage{$session}}{"duration"} += $delta;
                $queries{$session}{$queryNum}{"duration"} += $delta;
                $sessions{$session}{"duration"} += $delta;

                $pages{$session}{$querystr}{$page}{"views"} += 1;

                $pages{$session}{$querystr}{$page}{"clicks"} += $clicks;
                $queries{$session}{$queryNum}{"clicks"} += $clicks;
                $sessions{$session}{"clicks"} += $clicks;
             }
          } 
          else {
             # Warning: query appears the same, but results changed
             #print "Warning: query appears to be the same, but numfound different\n";
             #print "\t $querystr $lastQuery{$session} = $numfound $lastNumFound{$session}\n";
          }
       }
       else {
          # Same session, new query
          $queryNum++;

          $sessions{$session}{"queries"} += 1;
          $sessions{$session}{"pages"} += 1;

          $pages{$session}{$querystr}{$page}{"clicks"} = $clicks;
          $pages{$session}{$querystr}{$page}{"duration"} = 0;
          $pages{$session}{$querystr}{$page}{"views"} = 1;

          $queries{$session}{$queryNum}{"pages"} = 1;
          $queries{$session}{$queryNum}{"clicks"} = $clicks;
          $queries{$session}{$queryNum}{"duration"} = 0;
          $queries{$session}{$queryNum}{"numfound"} = $numfound;
          $queries{$session}{$queryNum}{"query"} = $query;
          $queries{$session}{$queryNum}{"querystr"} = $querystr;

          my @terms = split(/ /, $query);
          $queries{$session}{$queryNum}{"terms"} = scalar(@terms);

          my $boolop = 0;
          while ($query =~ m/( or | and | not )/gi) {
             $boolop++;
          }
          $queries{$session}{$queryNum}{"boolop"} = $boolop;

          my $expr = 0;
          while ($query =~ m/\((.* and .*|.* or .*|.* not .*[^\)]*)\)/gi) {
             $expr++;
          }
          $queries{$session}{$queryNum}{"expr"} = $expr;

          my $phrases = 0;
          while ($query =~ m/"([^"]*)"/gi) {
             $phrases++;
          }
          $queries{$session}{$queryNum}{"phrases"} = $phrases;

          my $required = 0;
          while ($query =~ m/(\s-\w)/gi) {
             $required++;
          }
          $queries{$session}{$queryNum}{"required"} = $required;

          my $prohibited = 0;
          while ($query =~ m/(\s+\w)/gi) {
             $prohibited++;
          }
          $queries{$session}{$queryNum}{"prohibited"} = $prohibited;

          my $wild = 0;
          for ($i = 0; $i<length($query); $i++) {
             my $char = substr($query, $i, 1);
             if ($char eq "!") { $wild++; }
             if ($char eq "*") { $wild++; }
             if ($char eq "?") { $wild++; }
          }
          $queries{$session}{$queryNum}{"wild"} = $wild;

          $queries{$session}{$queryNum}{"topic"} = $topic;
          $queries{$session}{$queryNum}{"author"} = $author;
          $queries{$session}{$queryNum}{"language"} = $language;
          $queries{$session}{$queryNum}{"country"} = $country;
          $queries{$session}{$queryNum}{"pubdate"} = $pubdate;
          $queries{$session}{$queryNum}{"pubtrie"} = $pubtrie;
          $queries{$session}{$queryNum}{"format"} = $format;
          $queries{$session}{$queryNum}{"htsource"} = $htsource;
          $queries{$session}{$queryNum}{"lmt"} = $lmt;


          # How long was spent looking at the last page and query?
          $delta = $seconds - $lastSeconds{$session};
          $pages{$session}{$lastQuery{$session}}{$lastPage{$session}}{"duration"} += $delta;
          $queries{$session}{$queryNum-1}{"duration"} += $delta;
          $sessions{$session}{"duration"} += $delta;
       }
    }
    else { 
       # New session
       $queryNum = 0;
       $sessions{$session}{"queries"} = 1;
       $sessions{$session}{"pages"} = 1;

       $queries{$session}{$queryNum}{"pages"} = 1;
       $queries{$session}{$queryNum}{"clicks"} = $clicks;
       $queries{$session}{$queryNum}{"duration"} = 0;
       $queries{$session}{$queryNum}{"numfound"} = $numfound; 
       $queries{$session}{$queryNum}{"querystr"} = $querystr; 
       $queries{$session}{$queryNum}{"query"} = $query; 

          my @terms = split(/ /, $query);
          $queries{$session}{$queryNum}{"terms"} = scalar(@terms);

          my $boolop = 0;
          while ($query =~ m/( or | and | not )/gi) {
             $boolop++;
          }
          $queries{$session}{$queryNum}{"boolop"} = $boolop;

          my $expr = 0;
          while ($query =~ m/\((.* and .*|.* or .*|.* not .*[^\)]*)\)/gi) {
             $expr++;
          }
          $queries{$session}{$queryNum}{"expr"} = $expr;

          my $phrases = 0;
          while ($query =~ m/"([^"]*)"/gi) {
             $phrases++;
          }
          $queries{$session}{$queryNum}{"phrases"} = $phrases;

          my $required = 0;
          while ($query =~ m/(\s-\w)/gi) {
             $required++;
          }
          $queries{$session}{$queryNum}{"required"} = $required;

          my $prohibited = 0;
          while ($query =~ m/(\s+\w)/gi) {
             $prohibited++;
          }
          $queries{$session}{$queryNum}{"prohibited"} = $prohibited;

          my $wild = 0;
          for ($i = 0; $i<length($query); $i++) {
             my $char = substr($query, $i, 1);
             if ($char eq "!") { $wild++; }
             if ($char eq "*") { $wild++; }
             if ($char eq "?") { $wild++; }
          }
          $queries{$session}{$queryNum}{"wild"} = $wild;

          $queries{$session}{$queryNum}{"topic"} = $topic;
          $queries{$session}{$queryNum}{"author"} = $author;
          $queries{$session}{$queryNum}{"language"} = $language;
          $queries{$session}{$queryNum}{"country"} = $country;
          $queries{$session}{$queryNum}{"pubdate"} = $pubdate;
          $queries{$session}{$queryNum}{"pubtrie"} = $pubtrie;
          $queries{$session}{$queryNum}{"format"} = $format;
          $queries{$session}{$queryNum}{"htsource"} = $htsource;
          $queries{$session}{$queryNum}{"lmt"} = $lmt;


       $pages{$session}{$querystr}{$page}{"views"} = 1;
       $pages{$session}{$querystr}{$page}{"clicks"} = $clicks;
       $pages{$session}{$querystr}{$page}{"duration"} = 0;

       #$lastSession{$session} = 1;
       #$lastQuery{$session} = $querystr;
       #$lastPage{$session} = $page;
       #$lastSeconds{$session} = $seconds;
       #$lastNumFound{$session} = $numfound;

       # Calculate last session, query, and page duration
       #$delta = $seconds - $lastSeconds;
       #if ($lastSession{$session} == 1) {
       #  $pages{$lastSession}{$lastQuery}{$lastPage}{"duration"} += $delta;
       #  $queries{$lastSession}{$lastQuery}{"duration"} += $delta;
       #  $sessions{$lastSession}{"duration"} += $delta;
       #}
    }
    $lastSession{$session} = 1;
    $lastQuery{$session} = $querystr;
    $lastNumFound{$session} = $numfound;
    $lastPage{$session} = $page;
    $lastSeconds{$session} = $seconds;
}
close(SOLR);


#print "\nSessions:\n";
#for $session (keys %sessions) {
#    my $queries = $sessions{$session}{"queries"};
#    my $duration = $sessions{$session}{"duration"};
#    my $clicks = $sessions{$session}{"clicks"};
#    my $pages = $sessions{$session}{"pages"};
#    print "session=$session, duration=$duration, queries=$queries, clicks=$clicks, pages=$pages\n";
#}

print "session|querynum|query|numfound|pages|duration|clicks|terms|phrases|boolop|expr|wild|topic|author|language|country|pubdate|pubtrie|format|htsource|lmt|incommon|tophrase|addterms|rmterms|editdist|addop|addfacet|rmfacet|addft|rmft\n";
for $session (keys %queries) {
   foreach $querynum (sort {$a <=> $b}keys %{$queries{$session}}) {
      my $duration = $queries{$session}{$querynum}{"duration"};
      my $clicks = $queries{$session}{$querynum}{"clicks"};
      my $pages = $queries{$session}{$querynum}{"pages"};
      my $numfound = $queries{$session}{$querynum}{"numfound"};
      my $terms = $queries{$session}{$querynum}{"terms"};
      my $phrases = $queries{$session}{$querynum}{"phrases"};
      my $boolop = $queries{$session}{$querynum}{"boolop"};
      my $expr = $queries{$session}{$querynum}{"expr"};
      my $wild = $queries{$session}{$querynum}{"wild"};
      my $query = $queries{$session}{$querynum}{"query"};
      my $topic = ($queries{$session}{$querynum}{"topic"} ne "") ? 1 : 0;
      my $author = ($queries{$session}{$querynum}{"author"} ne "") ? 1 : 0;
      my $language = ($queries{$session}{$querynum}{"language"} ne "") ? 1 : 0;
      my $country = ($queries{$session}{$querynum}{"country"} ne "") ? 1 : 0;
      my $pubdate = ($queries{$session}{$querynum}{"pubdate"} ne "") ? 1 : 0;
      my $pubtrie = ($queries{$session}{$querynum}{"pubtrie"} ne "") ? 1 : 0;
      my $format = ($queries{$session}{$querynum}{"format"} ne "") ? 1 : 0;
      my $htsource = ($queries{$session}{$querynum}{"htsource"} ne "") ? 1 : 0;
      my $lmt = ($queries{$session}{$querynum}{"lmt"} ne "") ? 1 : 0;
      #$query = lc($query);

      my $newquery = 0;
      my $tophrase = 0;
      my $rmphrase = 0;
      my $addterms = 0;
      my $rmterms = 0;
      my $editdist = 0;
      my $addop = 0;
      my $addfacet = 0;
      my $rmfacet = 0;
      my $addft = 0;
      my $rmft = 0;
      my $incommon = 0;
      my $numfounddiff = 0;
      my $clickdiff = 0;
      # What's the difference between this and the last quer     
      if ($querynum > 0) {
          my $lastduration = $queries{$session}{$querynum-1}{"duration"};
          my $lastclicks = $queries{$session}{$querynum-1}{"clicks"};
          my $lastpages = $queries{$session}{$querynum-1}{"pages"};
          my $lastnumfound = $queries{$session}{$querynum-1}{"numfound"};
          my $lastterms = $queries{$session}{$querynum-1}{"terms"};
          my $lastquery= $queries{$session}{$querynum-1}{"query"};
          $lastquery= lc($lastquery);

          $numfounddiff = $numfound - $lastnumfound;
          $clickdiff = $clicks - $lastclicks;
          if ($query ne $lastquery) {
             # Change to phrase search
             if ($query =~ /"/) {
                $lastquery =~ s/\)/\\\)/g;
                $lastquery =~ s/\(/\\\(/g;
                $lastquery =~ s/\+/\\\+/g;
                $tmp = $query;
                $tmp =~ s/"//g;
                if ($tmp =~ /^$lastquery$/) {
                   $tophrase = 1;
                }
             }
             if ($lastquery =~ /"/) {
                $tmp2 = $query;
                $tmp2 =~ s/\./\\\./g;
                $tmp2 =~ s/\)/\\\)/g;
                $tmp2 =~ s/\(/\\\(/g;
                $tmp = $lastquery;
                $tmp =~ s/\)/\\\)/g;
                $tmp =~ s/\(/\\\(/g;
                $tmp =~ s/"//g;
                if ($tmp =~ /^$tmp2$/) {
                   $rmphrase = 1;
                }
             }
             # Distance 
             $editdist = distance($query, $lastquery);
  
             #terms in common
             my $tmpq = $query;
             my $tmplq = $lastquery;
             $tmpq =~ s/"//g;
             $tmplq =~ s/"//g;
             my @terms = split / /, $tmpq;
             my @lastterms = split / /, $tmplq;

             $incommon = common(\@terms, \@lastterms);
             if (scalar(@terms) > scalar(@lastterms) && $incommon > 0) {
                $addterms++;
             }
             if (scalar(@terms) < scalar(@lastterms) && $incommon > 0) {
                $rmterms++;
             }
             if (scalar(@terms) == scalar(@lastterms) && $incommon > 0 && $incommon < scalar(@terms)) {
                $addterms++;
             }
             if ($incommon == 0) {
                $newquery = 1;
             }
          }


          my $lastphrases = $queries{$session}{$querynum-1}{"phrases"};
          my $lastboolop = $queries{$session}{$querynum-1}{"boolop"};
          my $lastexpr = $queries{$session}{$querynum-1}{"expr"};
          my $lastwild = $queries{$session}{$querynum-1}{"wild"};
          my $lasttopic = ($queries{$session}{$querynum-1}{"topic"} ne "") ? 1 : 0;
          my $lastauthor = ($queries{$session}{$querynum-1}{"author"} ne "") ? 1 : 0;
          my $lastlanguage = ($queries{$session}{$querynum-1}{"language"} ne "") ? 1 : 0;
          my $lastcountry = ($queries{$session}{$querynum-1}{"country"} ne "") ? 1 : 0;
          my $lastpubdate = ($queries{$session}{$querynum-1}{"pubdate"} ne "") ? 1 : 0;
          my $lastpubtrie = ($queries{$session}{$querynum-1}{"pubtrie"} ne "") ? 1 : 0;
          my $lastformat = ($queries{$session}{$querynum-1}{"format"} ne "") ? 1 : 0;
          my $lasthtsource = ($queries{$session}{$querynum-1}{"htsource"} ne "") ? 1 : 0;
          my $lastlmt = ($queries{$session}{$querynum-1}{"lmt"} ne "") ? 1 : 0;

          if ($boolop != $lastboolop) { $addop = 1; }
          if ($expr != $lastexpr) { $addop = 1; }
          if ($wild != $lastwild) { $addop = 1; }

          if ($topic > $lasttopic) { $addfacet = 1; }
          if ($author > $lastauthor) { $addfacet = 1; }
          if ($language > $lastlanguage) { $addfacet = 1; }
          if ($country > $lastcountry) { $addfacet = 1; }
          if ($pubdate > $lastpubdate) { $addfacet = 1; }
          if ($pubtrie > $lastpubtrie) { $addfacet = 1; }
          if ($format > $lastformat) { $addfacet = 1; }
          if ($htsource > $lasthtsource) { $addfacet = 1; }

          if ($topic < $lasttopic) { $rmfacet = 1; }
          if ($author < $lastauthor) { $rmfacet = 1; }
          if ($language < $lastlanguage) { $rmfacet = 1; }
          if ($country < $lastcountry) { $rmfacet = 1; }
          if ($pubdate < $lastpubdate) { $rmfacet = 1; }
          if ($pubtrie < $lastpubtrie) { $rmfacet = 1; }
          if ($format < $lastformat) { $rmfacet = 1; }
          if ($htsource < $lasthtsource) { $rmfacet = 1; }
  
          if ($lmt >$lastlmt) { $addft = 1; }
          if ($lmt <$lastlmt) { $rmft = 1; }
      }


      print "$session|$querynum|$query|$numfound|$pages|$duration|$clicks|$terms|$phrases|$boolop|$expr|$wild|$topic|$author|$language|$country|$pubdate|$pubtrie|$format|$htsource|$lmt|$incommon|$newquery|$tophrase|$rmphrase|$addterms|$rmterms|$editdist|$addop|$addfacet|$rmfacet|$addft|$rmft\n";
 
   }
}

#print "Page\n";
#for $session (keys %pages) {
#   for $query (sort keys %{$pages{$session}}) {
##      for $page (sort {$a <=> $b}  keys %{$pages{$session}{$query}}) {
#          my $duration = $pages{$session}{$query}{$page}{"duration"};
#          my $clicks = $pages{$session}{$query}{$page}{"clicks"};
#          my $views = $pages{$session}{$query}{$page}{"views"};
#          print "session=$session, query=$query, page=$page, duration=$duration, clicks=$clicks, views=$views\n";
#      }
#   }
#}


sub common() {
   my ($words1, $words2) = @_;
   my $common = 0;
   foreach $w1 (@{$words1}) {
      foreach $w2 (@{$words2}) {
          if ($w1 eq $w2) {
             $common++;
          }
      }
   }
   return $common;
}
