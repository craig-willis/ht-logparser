#!/usr/bin/perl

use utf8;

#
# Calculate transition probabilities and output for 
# rendering in graphviz.
#


my %sessions;
my %queries;
while (<>) 
{
    #session|querynum|query|numfound|pages|duration|clicks|terms|phrases|boolop|expr|wild|topic|author|language|country|pubdate|pubtrie|format|htsource|rights|incommon|tophrase|addterms|editdist|addop|addfacet|rmfacet|addft|rmft\n";
    $_ =~ s/\n//g;
    my @fields = split (/\|/, $_);
    my $session = $fields[0]; 
    my $querynum = $fields[1]; 
    $sessions{$session}{$querynum} = $_;
}

my $START = 0;
my $REFORM = 1;
my $FT = 2;
my $NEW_QUERY = 3;
my $FACET = 4;
my $END = 5;

#my $START = 0;
#my $PHRASE = 1;
#my $TERM = 2;
#my $OP = 3;
#my $FACET = 4;
#my $FT = 5;
#my $NEW_QUERY = 6;
#my $CLICK = 7;
#my $END = 8;

#print "session|querynum|query|terms|incommon|newquery|tophrase|rmphrase|addterms|rmterms|addop|addfacet|rmfacet|addft|rmft|editdist|clicks|numfound\n";
my $lastType = $START;
my %transitions;
my $lastnumfound = 0;
for $session (keys %sessions) {
  my @queries = keys %{$sessions{$session}};
  my $numqueries = scalar(@queries);
  for $querynum (sort {$a <=> $b} keys %{$sessions{$session}}) {
      my $record = $sessions{$session}{$querynum};
      my @fields = split (/\|/, $record);
      my $query = $fields[2]; 
      my $numfound = $fields[3]; 
      my $pages = $fields[4]; 
      my $duration = $fields[5]; 
      my $clicks = $fields[6]; 
      my $terms = $fields[7]; 
      my $incommon = $fields[21]; 
      my $newquery = $fields[22]; 
      my $tophrase = $fields[23]; 
      my $rmphrase = $fields[24]; 
      my $addterms = $fields[25]; 
      my $rmterms = $fields[26]; 
      my $editdist = $fields[27]; 
      my $addop = $fields[28]; 
      my $addfacet = $fields[29]; 
      my $rmfacet = $fields[30]; 
      my $addft = $fields[31]; 
      my $rmft = $fields[32]; 


#print "$session|$querynum|$query|$terms|$incommon|$newquery|$tophrase|$rmphrase|$addterms|$rmterms|$addop|$addfacet|$rmfacet|$addft|$rmft|$editdist|$clicks|$numfound\n";

      my $numfounddiff = $numfound - $lastnumfound;
      if ($querynum > 0) {
         #if ($tophrase == 1 || $rmphrase == 1) { $transitions{$lastType}{$PHRASE}++ };
         #if ($addterms == 1 || $rmterms == 1) { $transitions{$lastType}{$TERM}++ };
         #if ($addterms == 1 || $rmterms == 1) { $transitions{$lastType}{$TERM}++ };
         #if ($addop == 1) { $transitions{$lastType}{$OP}++ };
         #if ($addfacet == 1 || $rmfacet == 1) { $transitions{$lastType}{$FACET}++ };
         #if ($clicks >0) { $transitions{$lastType}{$CLICK}++ };

         if ($addterms == 1 || $rmterms == 1) { $transitions{$lastType}{$REFORM}++ };
         if ($addop == 1) { $transitions{$lastType}{$REFORM}++ };
         if ($tophrase == 1 || $rmphrase == 1) { $transitions{$lastType}{$REFORM}++ };
         if ($addfacet == 1 || $rmfacet == 1) { $transitions{$lastType}{$FACET}++ };
         if ($addft == 1 || $rmft == 1) { $transitions{$lastType}{$FT}++ };
         if ($newquery == 1) { $transitions{$lastType}{$NEW_QUERY}++ };

      }

      if ($querynum > 0) {
         if ($addfacet == 1 || $rmfacet == 1) { $lastType = $FACET };
         if ($addft == 1 || $rmft == 1) {    $lastType = $FT };

         #if ($tophrase == 1 || $rmphrase == 1) { $lastType = $PHRASE };
         #if ($addterms == 1 || $rmterms == 1) { $lastType = $TERM };
         #if ($addop == 1) {    $lastType = $OP };
         if ($tophrase == 1 || $rmphrase == 1) { $lastType = $REFORM };
         if ($addterms == 1 || $rmterms == 1) { $lastType = $REFORM };
         if ($addop == 1) {    $lastType = $REFORM };
         if ($newquery == 1) { $lastType = $NEW_QUERY };
      } else { 
         $lastType = $START;
      }
      $lastnumfound = $numfound;
   }

   $lastnumfound = 0;
   $transitions{$lastType}{$END}++; 
}

my %totals;
my $total;
for $from (sort {$a <=> $b} keys %transitions) {
   for $to (sort {$a <=> $b} keys %{$transitions{$from}}) {
      my $cnt = $transitions{$from}{$to};
      if (!$totals{$from}) {
         $totals{$from} += $0;
      }
      $totals{$from} += $cnt;
      $total += $cnt;
   }
}


for ($from = $START; $from < $END; $from ++) {
   my $base = $totals{$from};
   print "$from";
   for ($to = $START ; $to <= $END ; $to ++) {
      my $cnt = $transitions{$from}{$to};
      if ($base > 0) {
         my $pct = sprintf "%.3f", ($cnt / $base);
         print ",$pct";
      } else {
         print ",0.00";
      }
   }
   print "\n";
}

print "-----\n";
for ($from = $START; $from < $END; $from ++) {
   my $base = $totals{$from};
   print "$from";
   for ($to = $START ; $to <= $END ; $to ++) {
      my $cnt = $transitions{$from}{$to};
      print ",$cnt";
   }
   print "\n";
}

print "-----\n";

$labels[0] = "Start";
$labels[1] = "Reformulate query";
$labels[2] = "Toggle FT";
$labels[3] = "New Query";
$labels[4] = "Add/Remove Facet";
$labels[5] = "End";

#$labels[0] = "Start";
#$labels[1] = "Add/Remove Phrase";
#$labels[2] = "Add/Remove Term";
#$labels[3] = "Add/Remove Operator";
#$labels[4] = "Add/Remove Facet";
#$labels[5] = "Toggle FT";
#$labels[6] = "New Query";
#$labels[7] = "Clickthru";
#$labels[8] = "End";
for ($from = $START; $from < $END; $from ++) {
   my $base = $totals{$from};
   for ($to = $START ; $to <= $END ; $to ++) {
      my $cnt = $transitions{$from}{$to};
      if ($base > 0) {
         $pct = sprintf "%.2f", ($cnt / $base);
      } else {
         $pct = "0.00";
      }
      if ($pct ne "0.00") {
         print  "\"" . $labels[$from] . "\" -> \"" . $labels[$to] . "\" [label = \"$pct\" ];\n";
      }
   }
   print "\n";
}
