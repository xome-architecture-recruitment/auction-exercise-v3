namespace AuctionBids.Store
{
    public class AuctionState
    {
        public decimal StartingPrice { get; set; } = 250000m;
        public decimal ReservePrice { get; set; } = 275000m;
        public decimal CurrentPrice { get; set; } = 250000m;
        public DateTime ClosingTime { get; set; } = DateTime.UtcNow.AddHours(24);

        public Dictionary<string, decimal> MaximunBids { get; set; } = new Dictionary<string, decimal>();
        public Dictionary<string, object> ProcessedRequests { get; set; } = new Dictionary<string, object>();
        public string? LeadingBidderId { get; set; } = null;
    }
}
