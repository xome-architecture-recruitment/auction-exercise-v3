namespace AuctionBids.Models
{
    public class AuctionStatusResponse
    {
        public decimal StartingPrice { get; set; }
        public decimal CurrentPrice { get; set; }
        public string? LeadingBidderId { get; set; }
        public bool ReserveMet { get; set; }
        public DateTime ClosingTime { get; set; }
        public bool IsClosed { get; set; }
    }
}
