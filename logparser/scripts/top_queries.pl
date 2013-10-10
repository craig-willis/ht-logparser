#!/usr/bin/perl



my %top;
my %queries;
while (<>)
{
   $_ =~ s/\n//g;
   my ($session, $queryNum, $query) = split /\|/, $_;
   my ($ipaddr, $session) = split / /, $session;
   $key = $ipaddr . "|" . lc($query);
   $query = lc($query);

   if ($queries{$key} eq "") {
      if ($top{$query} eq "") {
         $top{$query} = 1;
      } else {
         $top{$query}++;
      }
      $queries{$key} = 1;
   } 
}

for $key (sort {$top{$a} <=> $top{$b}} keys %top)
{ 
   print $key . "," . $top{$key} . "\n";
}

