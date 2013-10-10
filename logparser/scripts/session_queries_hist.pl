#!/usr/bin/perl

#
# Prints simple histogram
#

my %hist;
$total = 0;
while (<>) {
   $_ =~ s/\n//g;
   my ($session,$queries,$numfound,$pages,$duration,$clicks) = split /,/, $_;
   if ($hist{$queries} eq "" ) {
      $hist{$queries} = 1;
   } else { 
      $hist{$queries}++; 
   }
   $total++;
}


for $key (sort { $hist{$a} <=> $hist{$b} } keys %hist ) 
{
    print $key . "," , $hist{$key}/$total . "\n";
}
