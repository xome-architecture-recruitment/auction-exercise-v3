namespace AuctionBids.Models
{
    public class PlaceBidRequest
    {
        public string BidderId { get; set; }
        public decimal MaximumAmount { get; set; }
        public string RequestId { get; set; }
    }
}
