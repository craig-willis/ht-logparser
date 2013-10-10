#!/usr/bin/perl

my %top;
while(<>)
{
   $_ =~ s/\n//g;
   $_ = lc($_);
   if ($top{$_} eq "") {
      $top{$_} = 1;
   } else {
      $top{$_}++;
   }
}


for $key (sort {$top{$a} <=> $top{$b}} keys %top)
{ 
   print $key . "," . $top{$key} . "\n";
}
