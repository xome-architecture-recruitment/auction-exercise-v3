namespace AuctionBids.Models
{
    public class BidResponse
    {
        public bool Accepted { get; set; }
        public string Message { get; set; }
        public decimal CurrentPrice { get; set; }
        public string? LeadingBidderId { get; set; }
        public bool ReserveMet { get; set; }
    }
}
