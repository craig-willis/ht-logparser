#!/usr/bin/perl

my $total = 0;
while(<>) {
   $_ =~ s/\n//g;
   $total+= $_;
}
print "$total\n"
