using AuctionBids.Models;

namespace AuctionBids.Services
{
    public interface IAuctionService
    {
        BidResponse PlaceBid(PlaceBidRequest request);
        AuctionStatusResponse GetStatus();
    }
}
