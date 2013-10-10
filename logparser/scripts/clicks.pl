#!/usr/bin/perl

#
# Estimates the number of clicks per query based on 
# a count of aligned full-text (pt) requests with 
# pn (page number) = 0;
#

my %ls_pt_map;
my %pt_ls_map;
open(PT_LS, "pt_ls.out");
while (<PT_LS>) {
    $_ =~ s/\n//g;
    my ($pt_id, $ls_id) = split(/,/, $_);
    # each ls can have many pts
    push(@{$ls_pt_map{$ls_id}}, $pt_id);
    # each pt has only one ls id
    $pt_ls_map{$pt_id} = $ls_id;
}
close(PT_LS);

my %solr_ls_map;
open(SOLR_LS, "solr_ls.out");
while (<SOLR_LS>) {
    $_ =~ s/\n//g;
    my ($solr_id, $ls_id) = split(/,/, $_);
    # each solr can have only one ls 
    $solr_ls_map{$solr_id} = $ls_id;
}
close(SOLR_LS);

my %pt_map;
open (PT, "pt.out");
while (<PT>) {
    $_ =~ s/\n//g;
    my ($pt_id, $ipaddr, $timestamp, $id, $q1, $pn, $seq, $attr, 
       $view, $orient, $page, $size, $start, $referer, $url) = split(/\|/, $_);
    if ($pn == 0 && $pt_ls_map{$pt_id} ) {
       $pt_map{$pt_id} = 1;
    }
}

print "solr_id|ls_id|clicks\n";
open(SOLR, "solr.out");
while(<SOLR>) {
   $_ =~ s/\n//g;
   my @fields = split(/\|/, $_); 
   my $solr_id = $fields[0];
   my $ls_id = $solr_ls_map{$solr_id};
   my @pt_ids = @{$ls_pt_map{$ls_id}};
   print "$solr_id|$ls_id|" . scalar(@pt_ids) . "\n";
}
