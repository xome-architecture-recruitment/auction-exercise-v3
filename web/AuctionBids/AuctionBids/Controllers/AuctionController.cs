using Microsoft.AspNetCore.Mvc;
using AuctionBids.Services;
using AuctionBids.Models;

namespace AuctionBids.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class AuctionController : ControllerBase
    {
        private readonly Services.IAuctionService _auctionService;
        public AuctionController(IAuctionService auctionService)
        {
            _auctionService = auctionService;
        }

        [HttpPost("bids")]
        public ActionResult<BidResponse> PlaceBid([FromBody] PlaceBidRequest request)
        {
            BidResponse response = _auctionService.PlaceBid(request);
            if (response.Accepted)
            {
                return Ok(response);
            }
            else
            {
                return BadRequest(response);
            }
        }

        [HttpGet("status")]
        public ActionResult<AuctionStatusResponse> GetStatus()
        {
            AuctionStatusResponse status = _auctionService.GetStatus();
            return Ok(status);
        }
    }
}
